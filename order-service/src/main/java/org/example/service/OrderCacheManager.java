package org.example.service;

import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemListDTO;
import org.example.dto.OrderListDTO;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderCacheManager {

    Mono<OrderListDTO> getOrderList(String cacheKey);

    Mono<OrderDetailDTO> getOrderDetail(String cacheKey);

    Mono<OrderItemListDTO> getOrderItems(String cacheKey);

    Mono<OrderListDTO> cacheOrderList(String cacheKey, UUID userId, Mono<OrderListDTO> data);

    Mono<OrderDetailDTO> cacheOrderDetail(String cacheKey, UUID userId, Mono<OrderDetailDTO> data);

    Mono<OrderItemListDTO> cacheOrderItems(String cacheKey, UUID userId, Mono<OrderItemListDTO> data);

    Mono<Void> clearCaches(UUID userId);

    String buildOrdersCacheKey(UUID userId, Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate);

    String buildOrderDetailCacheKey(Integer id, UUID userId);

    String buildOrderItemsCacheKey(Integer orderId, UUID userId, Pageable pageable);
}
