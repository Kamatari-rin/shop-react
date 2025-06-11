package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletDTO(
        UUID userId,
        BigDecimal balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt)
{}