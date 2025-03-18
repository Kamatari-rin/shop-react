package org.example.dto;

import java.math.BigDecimal;

public record CartItemDTO(
        Integer productId,
        String productName,
        BigDecimal priceAtTime,
        Integer quantity,
        String imageUrl
) {}