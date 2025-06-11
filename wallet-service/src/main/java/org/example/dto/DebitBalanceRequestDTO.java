package org.example.dto;

import java.math.BigDecimal;

public record DebitBalanceRequestDTO(
        BigDecimal amount)
{}