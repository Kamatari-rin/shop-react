package org.example.dto;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record OrderItemDTO(
        Integer id,
        Integer productId,
        String productName,
        @Min(value = 1, message = "Quantity must be greater than 0") Integer quantity,
        BigDecimal price,
        String imageUrl
) {}