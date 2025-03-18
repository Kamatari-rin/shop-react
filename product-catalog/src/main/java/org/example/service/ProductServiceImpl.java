package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDTO;
import org.example.mapper.ProductMapper;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDTO> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable) {
        String searchPattern = search != null ? "%" + search.toLowerCase() + "%" : null;
        return productRepository.findProducts(categoryId, minPrice, maxPrice, search, searchPattern, pageable)
                .map(productMapper::toDto);
    }
}