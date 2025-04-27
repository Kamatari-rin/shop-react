package org.example.model;

import io.r2dbc.spi.Row;
import lombok.NonNull;
import org.example.enums.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("orders")
public record Order(
        @Id Integer id,
        @NonNull @Column("user_id") UUID userId,
        @NonNull @Column("order_date") LocalDateTime orderDate,
        @NonNull OrderStatus status,
        @Column("total_amount") BigDecimal totalAmount
) {
    public static Order fromRow(Row row) {
        return new Order(
                row.get("id", Integer.class),
                row.get("user_id", UUID.class),
                row.get("order_date", LocalDateTime.class),
                row.get("status", OrderStatus.class),
                row.get("total_amount", BigDecimal.class)
        );
    }
}