package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDTO(
        Integer id,
        UUID userId,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount
) {}