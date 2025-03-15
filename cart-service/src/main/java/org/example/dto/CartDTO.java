package org.example.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartDTO(
        Integer id,
        UUID userId,
        List<CartItemDTO> items,
        BigDecimal totalAmount
) {}