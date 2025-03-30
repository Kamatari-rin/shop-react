package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartDTO(
        Integer id,
        UUID userId,
        List<CartItemDTO> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
    public record CartItemDTO(
            Integer id,
            Integer productId,
            String productName,
            BigDecimal priceAtTime,
            Integer quantity,
            String imageUrl
    ) {}
}