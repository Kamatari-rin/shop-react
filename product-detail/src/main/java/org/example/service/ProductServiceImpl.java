package org.example.service;

import org.example.dto.ProductDetailDTO;
import org.example.exception.ProductNotFoundException;
import org.example.mapper.ProductMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

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
}