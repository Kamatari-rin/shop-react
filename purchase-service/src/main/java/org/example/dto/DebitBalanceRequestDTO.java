package org.example.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DebitBalanceRequestDTO(
        @Positive
        BigDecimal amount)
{}