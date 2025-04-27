package org.example.model;

import io.r2dbc.spi.Row;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_items")
public record OrderItem(
        @Id Integer id,
        @NonNull @Column("order_id") Integer orderId,
        @NonNull @Column("product_id") Integer productId,
        @NonNull Integer quantity,
        BigDecimal price,
        @Column("image_url") String imageUrl
) {
    public static OrderItem fromRow(Row row) {
        return new OrderItem(
                row.get("id", Integer.class),
                row.get("order_id", Integer.class),
                row.get("product_id", Integer.class),
                row.get("quantity", Integer.class),
                row.get("price", BigDecimal.class),
                row.get("image_url", String.class)
        );
    }
}