package org.example.service;


import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemListDTO;
import org.example.dto.OrderListDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderService {
    Mono<OrderListDTO> getOrders(UUID userId, Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate);
    Mono<OrderDetailDTO> getOrderDetail(Integer id, UUID userId);
    Mono<OrderItemListDTO> getOrderItems(Integer orderId, UUID userId, Pageable pageable);
    Mono<OrderDetailDTO> createOrder(UUID userId, CreateOrderRequestDTO request);
    Mono<Void> deleteOrder(Integer id, UUID userId);
}