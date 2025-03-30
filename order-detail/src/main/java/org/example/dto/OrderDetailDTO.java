package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailDTO(
        Integer id,
        UUID userId,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount,
        List<OrderItemDTO> items
) {
    public record OrderItemDTO(
            Integer id,
            Integer productId,
            String productName,
            Integer quantity,
            BigDecimal price,
            String imageUrl
    ) {}
}