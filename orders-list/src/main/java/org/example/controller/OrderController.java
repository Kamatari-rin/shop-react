package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDTO;
import org.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders/list")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(@RequestHeader("X-User-Id") UUID userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequestDTO request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}