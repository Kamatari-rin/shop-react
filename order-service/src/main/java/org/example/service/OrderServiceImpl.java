package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.ProductClient;
import org.example.dto.*;
import org.example.enums.OrderStatus;
import org.example.exception.OrderNotFoundException;
import org.example.exception.ProductNotFoundException;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.repository.OrderItemRepository;
import org.example.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final OrderCacheManager cacheManager;

    @Override
    public Mono<OrderListDTO> getOrders(UUID userId, Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate) {
        String effectiveStatus = (status == null || status.trim().isEmpty()) ? null : status;
        String cacheKey = cacheManager.buildOrdersCacheKey(userId, pageable, effectiveStatus, startDate, endDate);

        return cacheManager.getOrderList(cacheKey)
                .switchIfEmpty(Mono.defer(() -> {
                    String orderBy = createOrderBy(pageable.getSort());
                    long offset = pageable.getOffset();
                    int limit = pageable.getPageSize();

                    Flux<Order> orders = orderRepository.findOrders(userId, effectiveStatus, startDate, endDate, orderBy, offset, limit);
                    Mono<Long> totalElements = orderRepository.countOrders(userId, effectiveStatus, startDate, endDate);

                    return orders
                            .map(orderMapper::toListDto)
                            .collectList()
                            .zipWith(totalElements)
                            .map(tuple -> createPage(tuple.getT1(), tuple.getT2(), pageable, OrderListDTO::new))
                            .flatMap(dto -> cacheManager.cacheOrderList(cacheKey, userId, Mono.just(dto)));
                }));
    }

    @Override
    public Mono<OrderDetailDTO> getOrderDetail(Integer id, UUID userId) {
        String cacheKey = cacheManager.buildOrderDetailCacheKey(id, userId);

        return cacheManager.getOrderDetail(cacheKey)
                .switchIfEmpty(Mono.defer(() -> orderRepository.findByIdAndUserId(id, userId)
                        .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                        .flatMap(order -> loadOrderItems(order)
                                .map(items -> orderMapper.toDto(order, items))
                                .flatMap(dto -> enrichItems(dto.items(), dto::id)
                                        .map(enrichedItems -> new OrderDetailDTO(dto.id(), dto.userId(), dto.orderDate(),
                                                dto.status(), dto.totalAmount(), enrichedItems))))
                        .flatMap(dto -> cacheManager.cacheOrderDetail(cacheKey, userId, Mono.just(dto)))));
    }

    @Override
    public Mono<OrderItemListDTO> getOrderItems(Integer orderId, UUID userId, Pageable pageable) {
        String cacheKey = cacheManager.buildOrderItemsCacheKey(orderId, userId, pageable);

        return cacheManager.getOrderItems(cacheKey)
                .switchIfEmpty(Mono.defer(() -> orderRepository.findByIdAndUserId(orderId, userId)
                        .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                        .flatMap(order -> orderItemRepository.findByOrderId(orderId, pageable.getOffset(), pageable.getPageSize())
                                .map(orderMapper::toItemDto)
                                .collectList()
                                .zipWith(orderItemRepository.countByOrderId(orderId))
                                .map(tuple -> createPage(tuple.getT1(), tuple.getT2(), pageable, OrderItemListDTO::new))
                                .flatMap(dto -> enrichItems(dto.items(), () -> null)
                                        .map(enrichedItems -> new OrderItemListDTO(enrichedItems, dto.page(), dto.size(),
                                                dto.totalPages(), dto.totalElements())))
                                .flatMap(dto -> cacheManager.cacheOrderItems(cacheKey, userId, Mono.just(dto))))));
    }

    @Override
    @Transactional
    public Mono<OrderDetailDTO> createOrder(CreateOrderRequestDTO request) {
        long uniqueProductIds = request.items().stream()
                .map(CreateOrderRequestDTO.OrderItemRequestDTO::productId)
                .distinct()
                .count();
        if (uniqueProductIds != request.items().size()) {
            throw new IllegalArgumentException("Duplicate product IDs in order request");
        }

        List<Integer> productIds = request.items().stream()
                .map(CreateOrderRequestDTO.OrderItemRequestDTO::productId)
                .toList();

        return productClient.getProductsByIds(productIds)
                .collectList()
                .flatMap(products -> {
                    Map<Integer, ProductDetailDTO> productMap = products.stream()
                            .collect(Collectors.toMap(ProductDetailDTO::id, p -> p));
                    List<OrderItem> orderItems = buildOrderItems(request, productMap);
                    BigDecimal totalAmount = calculateTotalAmount(orderItems);
                    Order order = new Order(null, request.userId(), LocalDateTime.now(), OrderStatus.PENDING, totalAmount);
                    return saveOrderWithItems(order, orderItems);
                })
                .doOnSuccess(dto -> cacheManager.clearCaches(request.userId()).subscribe());
    }

    @Override
    @Transactional
    public Mono<Void> deleteOrder(Integer id, UUID userId) {
        return orderRepository.findByIdAndUserId(id, userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> orderRepository.deleteByIdAndUserId(id, userId))
                .doOnSuccess(v -> cacheManager.clearCaches(userId).subscribe());
    }

    private Mono<List<OrderItem>> loadOrderItems(Order order) {
        return orderItemRepository.findByOrderId(order.id())
                .collectList();
    }

    private Mono<List<OrderItemDTO>> enrichItems(List<OrderItemDTO> items,
                                                 java.util.function.Supplier<Integer> orderIdSupplier) {
        List<Integer> productIds = items.stream()
                .map(OrderItemDTO::productId)
                .filter(id -> id != null)
                .toList();

        return productIds.isEmpty()
                ? Mono.just(items).doOnNext(i -> log.warn("No valid product IDs found for order {}", orderIdSupplier.get()))
                : productClient.getProductsByIds(productIds)
                .collectMap(ProductDetailDTO::id, ProductDetailDTO::name)
                .map(productNames -> items.stream()
                        .map(item -> new OrderItemDTO(
                                item.id(),
                                item.productId(),
                                item.productId() != null ? productNames.getOrDefault(item.productId(), "Unknown") : "Unknown",
                                item.quantity(),
                                item.price(),
                                item.imageUrl()))
                        .toList());
    }

    private String createOrderBy(Sort sort) {
        String orderBy = sort.stream()
                .map(order -> {
                    String property = switch (order.getProperty()) {
                        case "orderDate" -> "order_date";
                        case "totalAmount" -> "total_amount";
                        default -> order.getProperty();
                    };
                    return property + " " + (order.getDirection().isAscending() ? "ASC" : "DESC");
                })
                .collect(Collectors.joining(", "));
        return orderBy.isEmpty() ? "order_date DESC" : orderBy;
    }

    private <T, R> R createPage(List<T> content, long totalElements, Pageable pageable,
                                Function5<List<T>, Integer, Integer, Integer, Long, R> constructor) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return constructor.apply(content, pageNumber, pageSize, totalPages, totalElements);
    }

    private List<OrderItem> buildOrderItems(CreateOrderRequestDTO request, Map<Integer, ProductDetailDTO> productMap) {
        return request.items().stream()
                .map(item -> {
                    ProductDetailDTO product = productMap.get(item.productId());
                    if (product == null) {
                        throw new ProductNotFoundException(item.productId());
                    }
                    return new OrderItem(null, null, item.productId(), item.quantity(), product.price(), product.imageUrl());
                })
                .toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<OrderDetailDTO> saveOrderWithItems(Order order, List<OrderItem> orderItems) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    List<OrderItem> itemsWithOrderId = orderItems.stream()
                            .map(item -> new OrderItem(null, savedOrder.id(), item.productId(),
                                    item.quantity(), item.price(), item.imageUrl()))
                            .toList();
                    return orderItemRepository.saveAll(itemsWithOrderId)
                            .collectList()
                            .map(savedItems -> orderMapper.toDto(savedOrder, savedItems))
                            .flatMap(dto -> enrichItems(dto.items(), dto::id)
                                    .map(enrichedItems -> new OrderDetailDTO(dto.id(), dto.userId(), dto.orderDate(),
                                            dto.status(), dto.totalAmount(), enrichedItems)));
                });
    }

    @FunctionalInterface
    interface Function5<T1, T2, T3, T4, T5, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);
    }
}