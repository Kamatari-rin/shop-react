package org.example.dto;

import java.util.List;

public record OrderItemListDTO(
        List<OrderItemDTO> items,
        int page,
        int size,
        int totalPages,
        long totalElements
) {}