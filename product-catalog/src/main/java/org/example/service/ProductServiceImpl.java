package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ProductDTO;
import org.example.mapper.ProductMapper;
import org.example.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final R2dbcEntityTemplate template;
    private final ProductMapper productMapper;
    private final ReactiveRedisTemplate<String, ProductDTO> productRedisTemplate;
    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;

    private static final String PRODUCTS_CACHE = "products:catalog:";
    private static final String COUNT_CACHE = "products:count:";

    @Override
    public Mono<Page<ProductDTO>> getProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String search,
                                              Pageable pageable) {
        String searchPattern = search != null ? "%" + search.toLowerCase() + "%" : null;
        Pageable pageableWithDefaultSort = pageable.getSort().isEmpty()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("name").ascending())
                : pageable;

        String cacheKey = PRODUCTS_CACHE + generateCacheKey(categoryId, minPrice, maxPrice, searchPattern, pageableWithDefaultSort);
        String countKey = COUNT_CACHE + generateCacheKey(categoryId, minPrice, maxPrice, searchPattern, pageableWithDefaultSort);

        return getFromCache(cacheKey, countKey, pageableWithDefaultSort)
                .switchIfEmpty(fetchAndCacheFromDatabase(categoryId, minPrice, maxPrice, searchPattern,
                        pageableWithDefaultSort, cacheKey, countKey));
    }

    private Mono<Page<ProductDTO>> getFromCache(String cacheKey, String countKey, Pageable pageable) {
        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;

        return productRedisTemplate.opsForList().range(cacheKey, start, end)
                .collectList()
                .zipWith(stringRedisTemplate.opsForValue().get(countKey)
                        .map(Long::parseLong)
                        .defaultIfEmpty(0L))
                .filter(tuple -> !tuple.getT1().isEmpty())
                .doOnNext(tuple -> logger.info("Retrieved from Redis cache for key: {}", cacheKey))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    private Mono<Page<ProductDTO>> fetchAndCacheFromDatabase(Integer categoryId, BigDecimal minPrice,
                                                             BigDecimal maxPrice, String searchPattern,
                                                             Pageable pageable, String cacheKey, String countKey) {
        Criteria criteria = buildCriteria(categoryId, minPrice, maxPrice, searchPattern);

        Flux<ProductDTO> products = template.select(Product.class)
                .matching(Query.query(criteria)
                        .sort(pageable.getSort())
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset()))
                .all()
                .map(productMapper::toDto);

        Mono<Long> totalElements = template.count(Query.query(criteria), Product.class);

        return products.collectList()
                .zipWith(totalElements)
                .doOnNext(tuple -> logger.info("Fetched from database and caching for key: {}", cacheKey))
                .flatMap(tuple -> cacheResults(tuple.getT1(), tuple.getT2(), cacheKey, countKey, pageable));
    }

    private Criteria buildCriteria(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String searchPattern) {
        return Stream.of(
                        Optional.ofNullable(categoryId)
                                .map(id -> Criteria.where("category_id").is(id)),
                        Optional.ofNullable(minPrice)
                                .map(price -> Criteria.where("price").greaterThanOrEquals(price)),
                        Optional.ofNullable(maxPrice)
                                .map(price -> Criteria.where("price").lessThanOrEquals(price)),
                        Optional.ofNullable(searchPattern)
                                .map(pattern -> Criteria.where("name").like(pattern)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Criteria.empty(), Criteria::and, Criteria::and);
    }

    private Mono<Page<ProductDTO>> cacheResults(List<ProductDTO> products, Long totalElements,
                                                String cacheKey, String countKey, Pageable pageable) {
        if (products.isEmpty()) {
            return Mono.just(new PageImpl<>(products, pageable, totalElements));
        }

        return productRedisTemplate.opsForList()
                .rightPushAll(cacheKey, products)
                .then(productRedisTemplate.expire(cacheKey, Duration.ofMinutes(30)))
                .then(stringRedisTemplate.opsForValue()
                        .set(countKey, String.valueOf(totalElements), Duration.ofMinutes(30)))
                .thenReturn(new PageImpl<>(products, pageable, totalElements));
    }

    private String generateCacheKey(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                    String searchPattern, Pageable pageable) {
        return String.format("%s:%s:%s:%s:%d:%d:%s",
                categoryId != null ? categoryId : "null",
                minPrice != null ? minPrice.toPlainString() : "null",
                maxPrice != null ? maxPrice.toPlainString() : "null",
                searchPattern != null ? searchPattern.replace("%", "") : "null",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().stream()
                        .map(order -> order.getProperty() + "_" + order.getDirection())
                        .collect(Collectors.joining(",")));
    }
}