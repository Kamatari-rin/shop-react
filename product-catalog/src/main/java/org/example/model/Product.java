package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "products")
@Getter
@Builder
@AllArgsConstructor
public class Product {

    @Id
    private final Integer id;

    @Column("name")
    private final String name;

    @Column("price")
    private final BigDecimal price;

    @Column("image_url")
    private final String imageUrl;

    @Column("category_id")
    private final Integer categoryId;

    @Column("created_at")
    private final LocalDateTime createdAt;

    @Column("updated_at")
    private final LocalDateTime updatedAt;
}