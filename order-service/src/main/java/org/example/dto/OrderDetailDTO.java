package org.example.dto;

import org.example.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailDTO(
        Integer id,
        UUID userId,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemDTO> items
) {}