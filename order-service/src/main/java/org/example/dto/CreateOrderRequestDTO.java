package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequestDTO(
        @NotNull @Size(min = 1) List<OrderItemRequestDTO> items
) {
    public record OrderItemRequestDTO(
            @NotNull Integer productId,
            @Min(value = 1) Integer quantity
    ) {}
}