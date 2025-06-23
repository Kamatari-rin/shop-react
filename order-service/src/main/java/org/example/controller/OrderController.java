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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping
    public Mono<ResponseEntity<OrderListDTO>> getOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Fetching orders for user: {} with status: {}, startDate: {}, endDate: {}", userId, status, startDate, endDate);
        return orderService.getOrders(userId, pageable, status, startDate, endDate)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.ok(new OrderListDTO(List.of(), 0, pageable.getPageSize(), 0, 0)));
    }

    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderDetailDTO>> getOrderDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Min(1) Integer orderId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Fetching order with id: {} for user: {}", orderId, userId);
        return orderService.getOrderDetail(orderId, userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{orderId}/items")
    public Mono<ResponseEntity<OrderItemListDTO>> getOrderItems(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Min(1) Integer orderId,
            Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Fetching order items for order: {} and user: {}", orderId, userId);
        return orderService.getOrderItems(orderId, userId, pageable)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<OrderDetailDTO>> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrderRequestDTO request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Creating order for user: {}", userId);
        return orderService.createOrder(userId, request)
                .map(order -> ResponseEntity.status(HttpStatus.CREATED).body(order));
    }


    @DeleteMapping("/{orderId}")
    public Mono<ResponseEntity<Void>> deleteOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @Min(1) Integer orderId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Deleting order with id: {} for user: {}", orderId, userId);
        return orderService.deleteOrder(orderId, userId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}