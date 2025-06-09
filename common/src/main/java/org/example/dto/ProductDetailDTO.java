package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailDTO(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        Integer categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}