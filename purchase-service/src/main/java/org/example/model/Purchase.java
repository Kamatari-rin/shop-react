package org.example.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.enums.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("purchases")
public record Purchase(
        @Id
        Integer id,

        @NotNull
        @Column("order_id")
        Integer orderId,

        @NotNull
        UUID userId,

        @NotNull
        @Size(max = 50)
        PaymentStatus paymentStatus,

        @NotNull
        LocalDateTime transactionDate,

        @Size(max = 1000)
        String details
) {
}