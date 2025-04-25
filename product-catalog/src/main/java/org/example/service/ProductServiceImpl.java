package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDTO;
import org.example.mapper.ProductMapper;
import org.example.model.ProductPage;
import org.example.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Mono<ProductPage> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search, Pageable pageable) {
        String searchPattern = createSearchPattern(search);
        String orderBy = createOrderBy(pageable.getSort());
        long offset = pageable.getOffset();
        int limit = pageable.getPageSize();

        Mono<Long> totalElements = productRepository.countProducts(categoryId, minPrice, maxPrice, search, searchPattern);
        Flux<ProductDTO> products = productRepository.findProducts(categoryId, minPrice, maxPrice, search, searchPattern, orderBy, offset, limit)
                .map(productMapper::toDto);

        return products.collectList()
                .zipWith(totalElements)
                .map(tuple -> createProductPage(tuple.getT1(), tuple.getT2(), pageable));
    }

    private String createSearchPattern(String search) {
        return search != null ? "%" + search.toLowerCase() + "%" : null;
    }

    private String createOrderBy(Sort sort) {
        String orderBy = sort.stream()
                .map(order -> order.getProperty() + " " + (order.getDirection().isAscending() ? "ASC" : "DESC"))
                .collect(Collectors.joining(", "));
        return orderBy.isEmpty() ? "name ASC" : orderBy;
    }

    private ProductPage createProductPage(List<ProductDTO> content, long totalElements, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new ProductPage(content, pageNumber, pageSize, totalElements, totalPages);
    }
}