package org.example.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    private Integer id;

    @Column("cart_id")
    private UUID cartId;

    @Column("product_id")
    private Integer productId;

    private Integer quantity;

    @Column("price_at_time")
    private BigDecimal priceAtTime;
}