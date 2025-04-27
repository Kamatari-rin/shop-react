package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderListDTO(
        List<OrderDTO> orders,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
    public record OrderDTO(
            Integer id,
            UUID userId,
            LocalDateTime orderDate,
            String status,
            BigDecimal totalAmount
    ) {}
}