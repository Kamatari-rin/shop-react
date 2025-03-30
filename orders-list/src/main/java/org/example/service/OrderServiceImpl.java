package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDTO;
import org.example.mapper.OrderMapper;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDTO> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        Order order = Order.builder()
                .userId(request.userId())
                .orderDate(request.orderDate())
                .status(request.status())
                .totalAmount(request.totalAmount())
                .items(request.items().stream()
                        .map(item -> OrderItem.builder()
                                .productId(item.productId())
                                .quantity(item.quantity())
                                .price(item.price())
                                .imageUrl(item.imageUrl())
                                .build())
                        .toList())
                .build();

        order.getItems().forEach(item -> item.setOrder(order));
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }
}