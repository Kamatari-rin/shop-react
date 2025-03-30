package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.ProductClient;
import org.example.dto.OrderDetailDTO;
import org.example.exception.OrderNotFoundException;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductClient productClient;

    @Override
    public OrderDetailDTO getOrderDetail(Integer id, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new OrderNotFoundException(id));
        OrderDetailDTO dto = orderMapper.toDto(order);
        return enrichWithProductNames(dto);
    }

    private OrderDetailDTO enrichWithProductNames(OrderDetailDTO dto) {
        List<OrderDetailDTO.OrderItemDTO> enrichedItems = dto.items().stream()
                .map(item -> new OrderDetailDTO.OrderItemDTO(
                        item.id(),
                        item.productId(),
                        productClient.getProductById(item.productId()).name(),
                        item.quantity(),
                        item.price(),
                        item.imageUrl()))
                .collect(Collectors.toList());
        return new OrderDetailDTO(
                dto.id(),
                dto.userId(),
                dto.orderDate(),
                dto.status(),
                dto.totalAmount(),
                enrichedItems
        );
    }
}