package org.example.service;

import org.example.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductDTO> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable);
}