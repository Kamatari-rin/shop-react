package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Override
    @Cacheable(value = "orders", key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #status + '-' + #startDate + '-' + #endDate")
    public Mono<OrderListDTO> getOrders(UUID userId, Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate) {
        String orderBy = createOrderBy(pageable.getSort());
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        Flux<Order> orders = orderRepository.findOrders(userId, status, startDate, endDate, orderBy, offset, limit);
        Mono<Long> totalElements = orderRepository.countOrders(userId, status, startDate, endDate);

        return orders
                .map(orderMapper::toListDto)
                .collectList()
                .zipWith(totalElements)
                .map(tuple -> createPage(tuple.getT1(), tuple.getT2(), pageable, OrderListDTO::new));
    }

    @Override
    @Cacheable(value = "orders", key = "#id + '-' + #userId")
    public Mono<OrderDetailDTO> getOrderDetail(Integer id, UUID userId) {
        return orderRepository.findByIdAndUserId(id, userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> loadOrderItems(order)
                        .map(items -> orderMapper.toDto(order, items))
                        .flatMap(dto -> enrichItems(dto.items(), dto::id)
                                .map(enrichedItems -> new OrderDetailDTO(dto.id(), dto.userId(), dto.orderDate(),
                                        dto.status(), dto.totalAmount(), enrichedItems))));
    }

    @Override
    @Cacheable(value = "orderItems", key = "#orderId + '-' + #userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Mono<OrderItemListDTO> getOrderItems(Integer orderId, UUID userId, Pageable pageable) {
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        return orderRepository.findByIdAndUserId(orderId, userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(order -> orderItemRepository.findByOrderId(orderId, offset, limit)
                        .map(orderMapper::toItemDto)
                        .collectList()
                        .zipWith(orderItemRepository.countByOrderId(orderId))
                        .map(tuple -> createPage(tuple.getT1(), tuple.getT2(), pageable, OrderItemListDTO::new))
                        .flatMap(dto -> enrichItems(dto.items(), () -> null)
                                .map(enrichedItems -> new OrderItemListDTO(enrichedItems, dto.page(), dto.size(),
                                        dto.totalPages(), dto.totalElements()))));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"orders", "orderItems"}, key = "#request.userId()")
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
                });
    }

    @Override
    @Transactional
    @CacheEvict(value = {"orders", "orderItems"}, key = "#userId")
    public Mono<Void> deleteOrder(Integer id, UUID userId) {
        return orderRepository.findByIdAndUserId(id, userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> orderRepository.deleteByIdAndUserId(id, userId));
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