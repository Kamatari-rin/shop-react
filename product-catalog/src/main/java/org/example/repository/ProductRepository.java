package org.example.repository;

import org.example.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {
    @Query("""
            SELECT p.* FROM products p
            WHERE (:categoryId IS NULL OR p.category_id = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:search IS NULL OR LOWER(p.name) LIKE :searchPattern)
            ORDER BY :orderBy
            LIMIT :limit OFFSET :offset
            """)
    Flux<Product> findProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, String searchPattern, String orderBy, long offset, int limit);

    @Query("""
            SELECT COUNT(*) FROM products p
            WHERE (:categoryId IS NULL OR p.category_id = :categoryId)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (:search IS NULL OR LOWER(p.name) LIKE :searchPattern)
            """)
    Mono<Long> countProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, String searchPattern);
}