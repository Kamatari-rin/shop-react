package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDetailDTO;
import org.example.exception.ProductNotFoundException;
import org.example.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public Mono<ProductDetailDTO> getProductById(Integer id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .flatMap(product -> {
                    ProductDetailDTO dto = productMapper.toDto(product);
                    if (product.getCategoryId() == null) {
                        return Mono.just(dto);
                    }
                    return categoryRepository.findById(product.getCategoryId())
                            .map(category -> productMapper.updateCategoryName(dto, category.getName()))
                            .defaultIfEmpty(dto);
                });
    }

    @Override
    public Flux<ProductDetailDTO> getProductsByIds(List<Integer> ids) {
        return productRepository.findAllById(ids)
                .flatMap(product -> enrichWithCategory(productMapper.toDto(product), product.getCategoryId()));
    }

    private Mono<ProductDetailDTO> enrichWithCategory(ProductDetailDTO dto, Integer categoryId) {
        if (categoryId == null) {
            return Mono.just(dto);
        }
        return categoryRepository.findById(categoryId)
                .map(category -> productMapper.updateCategoryName(dto, category.getName()))
                .defaultIfEmpty(dto);
    }
}