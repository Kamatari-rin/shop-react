package org.example.service;

import org.example.model.ProductPage;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ProductService {
    Mono<ProductPage> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable);
}