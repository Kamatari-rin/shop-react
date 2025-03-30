package org.example.dto;

import java.math.BigDecimal;

public record ProductDTO(
        Integer id,
        String name,
        BigDecimal price,
        String imageUrl,
        Integer categoryId
) {}