package org.example.repository;

import org.example.model.Category;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CategoryRepository extends R2dbcRepository<Category, Integer> {
}