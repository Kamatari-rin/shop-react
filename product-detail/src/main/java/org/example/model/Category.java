package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("categories")
@Getter
@Setter
public class Category {
    @Id
    private Integer id;

    @Column("name")
    private String name;
}