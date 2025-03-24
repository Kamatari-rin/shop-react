package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequestDTO(
        UUID userId,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDTO> items
) {
    public record OrderItemDTO(
            Integer productId,
            Integer quantity,
            BigDecimal price,
            String imageUrl
    ) {}
}