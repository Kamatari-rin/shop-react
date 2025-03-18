package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseResponseDTO(
        Integer orderId,
        UUID userId,
        BigDecimal totalAmount,
        String paymentStatus,
        LocalDateTime transactionDate
) {}