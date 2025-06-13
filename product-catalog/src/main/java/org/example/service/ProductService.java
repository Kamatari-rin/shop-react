package org.example.service;

import org.example.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ProductService {
    Mono<Page<ProductDTO>> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable);
}