package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDetailDTO;
import org.example.exception.ProductNotFoundException;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ReactiveRedisTemplate<String, ProductDetailDTO> redisTemplate;

    private static final String PRODUCT_CACHE = "product:detail:";
    private static final String PRODUCTS_LIST_CACHE = "products:list:";

    @Override
    public Mono<ProductDetailDTO> getProductById(Integer id) {
        String key = PRODUCT_CACHE + id;
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                                .flatMap(product -> {
                                    ProductDetailDTO dto = productMapper.toDto(product);
                                    if (product.getCategoryId() == null) return Mono.just(dto);
                                    return categoryRepository.findById(product.getCategoryId())
                                            .map(category -> productMapper.updateCategoryName(dto, category.getName()))
                                            .defaultIfEmpty(dto);
                                })
                                .flatMap(dto -> redisTemplate.opsForValue()
                                        .set(key, dto, Duration.ofMinutes(30))
                                        .thenReturn(dto))
                );
    }

    @Override
    public Flux<ProductDetailDTO> getProductsByIds(List<Integer> ids) {
        String listKey = PRODUCTS_LIST_CACHE +
                ids.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));

        return redisTemplate.opsForList().range(listKey, 0, -1)
                .switchIfEmpty(
                        productRepository.findAllById(ids)
                                .flatMap(product -> enrichWithCategory(productMapper.toDto(product), product.getCategoryId()))
                                .collectList()
                                .flatMapMany(dtos -> redisTemplate.opsForList()
                                        .rightPushAll(listKey, dtos)
                                        .then(redisTemplate.expire(listKey, Duration.ofMinutes(30)))
                                        .thenMany(Flux.fromIterable(dtos)))
                );
    }

    private Mono<ProductDetailDTO> enrichWithCategory(ProductDetailDTO dto, Integer categoryId) {
        if (categoryId == null) return Mono.just(dto);
        return categoryRepository.findById(categoryId)
                .map(category -> productMapper.updateCategoryName(dto, category.getName()))
                .defaultIfEmpty(dto);
    }

    public Mono<ProductDetailDTO> updateProduct(Product product) {
        String key = PRODUCT_CACHE + product.getId();
        return productRepository.save(product)
                .flatMap(savedProduct -> {
                    ProductDetailDTO dto = productMapper.toDto(savedProduct);
                    if (savedProduct.getCategoryId() == null) return Mono.just(dto);
                    return categoryRepository.findById(savedProduct.getCategoryId())
                            .map(category -> productMapper.updateCategoryName(dto, category.getName()))
                            .defaultIfEmpty(dto);
                })
                .flatMap(dto -> redisTemplate.opsForValue()
                        .set(key, dto, Duration.ofMinutes(30))
                        .thenReturn(dto));
    }

    public Mono<Void> clearCache(Integer id) {
        String key = PRODUCT_CACHE + id;
        return redisTemplate.delete(key).then();
    }

    public Mono<Void> clearListCache() {
        return redisTemplate.scan(ScanOptions.scanOptions()
                        .match(PRODUCTS_LIST_CACHE + "*")
                        .build())
                .collectList()
                .flatMapMany(keys -> redisTemplate.delete(Flux.fromIterable(keys)))
                .then();
    }
}