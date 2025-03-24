package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderDetailDTO;
import org.example.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders/detail")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") UUID userId) {
        OrderDetailDTO order = orderService.getOrderDetail(id, userId);
        return ResponseEntity.ok(order);
    }
}