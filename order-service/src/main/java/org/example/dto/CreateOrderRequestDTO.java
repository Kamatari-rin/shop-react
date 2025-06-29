package org.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequestDTO(
        @NotNull UUID userId,
        @NonNull @Size(min = 1) List<OrderItemRequestDTO> items
) {
    public record OrderItemRequestDTO(
            @NonNull Integer productId,
            @Min(value = 1) Integer quantity
    ) {}
}