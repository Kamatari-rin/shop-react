package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemListDTO;
import org.example.dto.OrderListDTO;
import org.example.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<OrderListDTO>> getOrders(
            @PathVariable UUID userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        log.debug("Fetching orders for user: {} with status: {}, startDate: {}, endDate: {}", userId, status, startDate, endDate);
        return orderService.getOrders(userId, pageable, status, startDate, endDate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(new OrderListDTO(List.of(), 0, pageable.getPageSize(), 0, 0)));
    }

    @GetMapping("/{userId}/{orderId}")
    public Mono<ResponseEntity<OrderDetailDTO>> getOrderDetail(
            @PathVariable UUID userId,
            @PathVariable @Min(1) Integer orderId) {
        log.debug("Fetching order with id: {} for user: {}", orderId, userId);
        return orderService.getOrderDetail(orderId, userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{userId}/{orderId}/items")
    public Mono<ResponseEntity<OrderItemListDTO>> getOrderItems(
            @PathVariable UUID userId,
            @PathVariable @Min(1) Integer orderId,
            Pageable pageable) {
        log.debug("Fetching order items for order: {} and user: {}", orderId, userId);
        return orderService.getOrderItems(orderId, userId, pageable)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{userId}")
    public Mono<ResponseEntity<OrderDetailDTO>> createOrder(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateOrderRequestDTO request) {
        log.debug("Creating order for user: {}", userId);
        return orderService.createOrder(new CreateOrderRequestDTO(userId, request.items()))
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order));
    }

    @DeleteMapping("/{userId}/{orderId}")
    public Mono<ResponseEntity<Void>> deleteOrder(
            @PathVariable UUID userId,
            @PathVariable @Min(1) Integer orderId) {
        log.debug("Deleting order with id: {} for user: {}", orderId, userId);
        return orderService.deleteOrder(orderId, userId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}