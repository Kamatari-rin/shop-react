package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemListDTO;
import org.example.dto.OrderListDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCacheManagerImpl implements OrderCacheManager {

    private static final Logger log = LoggerFactory.getLogger(OrderCacheManagerImpl.class);
    private static final String ORDERS_CACHE_PREFIX = "orders:";
    private static final String ORDER_ITEMS_CACHE_PREFIX = "orderItems:";
    private static final String USER_CACHE_SET_PREFIX = "userCaches:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(5);

    private final ReactiveRedisTemplate<String, OrderListDTO> orderListRedisTemplate;
    private final ReactiveRedisTemplate<String, OrderDetailDTO> orderDetailRedisTemplate;
    private final ReactiveRedisTemplate<String, OrderItemListDTO> orderItemListRedisTemplate;
    private final ReactiveStringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<OrderListDTO> getOrderList(String cacheKey) {
        return orderListRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(value -> log.debug("Cache hit for key: {}", cacheKey))
                .onErrorResume(e -> {
                    log.error("Failed to access cache for key: {}", cacheKey, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<OrderDetailDTO> getOrderDetail(String cacheKey) {
        return orderDetailRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(value -> log.debug("Cache hit for key: {}", cacheKey))
                .onErrorResume(e -> {
                    log.error("Failed to access cache for key: {}", cacheKey, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<OrderItemListDTO> getOrderItems(String cacheKey) {
        return orderItemListRedisTemplate.opsForValue().get(cacheKey)
                .doOnNext(value -> log.debug("Cache hit for key: {}", cacheKey))
                .onErrorResume(e -> {
                    log.error("Failed to access cache for key: {}", cacheKey, e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<OrderListDTO> cacheOrderList(String cacheKey, UUID userId, Mono<OrderListDTO> data) {
        return data.flatMap(dto -> orderListRedisTemplate.opsForValue()
                .set(cacheKey, dto, CACHE_TTL)
                .then(stringRedisTemplate.opsForSet().add(USER_CACHE_SET_PREFIX + userId, cacheKey))
                .then(stringRedisTemplate.expire(USER_CACHE_SET_PREFIX + userId, CACHE_TTL))
                .thenReturn(dto)
                .onErrorResume(e -> {
                    log.warn("Failed to cache OrderListDTO for key: {}", cacheKey, e);
                    return Mono.just(dto);
                }));
    }

    @Override
    public Mono<OrderDetailDTO> cacheOrderDetail(String cacheKey, UUID userId, Mono<OrderDetailDTO> data) {
        return data.flatMap(dto -> orderDetailRedisTemplate.opsForValue()
                .set(cacheKey, dto, CACHE_TTL)
                .then(stringRedisTemplate.opsForSet().add(USER_CACHE_SET_PREFIX + userId, cacheKey))
                .then(stringRedisTemplate.expire(USER_CACHE_SET_PREFIX + userId, CACHE_TTL))
                .thenReturn(dto)
                .onErrorResume(e -> {
                    log.warn("Failed to cache OrderDetailDTO for key: {}", cacheKey, e);
                    return Mono.just(dto);
                }));
    }

    @Override
    public Mono<OrderItemListDTO> cacheOrderItems(String cacheKey, UUID userId, Mono<OrderItemListDTO> data) {
        return data.flatMap(dto -> orderItemListRedisTemplate.opsForValue()
                .set(cacheKey, dto, CACHE_TTL)
                .then(stringRedisTemplate.opsForSet().add(USER_CACHE_SET_PREFIX + userId, cacheKey))
                .then(stringRedisTemplate.expire(USER_CACHE_SET_PREFIX + userId, CACHE_TTL))
                .thenReturn(dto)
                .onErrorResume(e -> {
                    log.warn("Failed to cache OrderItemListDTO for key: {}", cacheKey, e);
                    return Mono.just(dto);
                }));
    }

    @Override
    public Mono<Void> clearCaches(UUID userId) {
        String setKey = USER_CACHE_SET_PREFIX + userId;
        return stringRedisTemplate.opsForSet().members(setKey)
                .flatMap(key -> stringRedisTemplate.delete(key)
                        .then(stringRedisTemplate.opsForSet().remove(setKey, key))
                        .doOnSuccess(v -> log.debug("Deleted cache key: {}", key)))
                .then()
                .onErrorResume(e -> {
                    log.error("Failed to clear caches for user: {}", userId, e);
                    return Mono.empty();
                });
    }

    @Override
    public String buildOrdersCacheKey(UUID userId, Pageable pageable, String status, LocalDateTime startDate, LocalDateTime endDate) {
        return ORDERS_CACHE_PREFIX + userId + "-" + pageable.getPageNumber() + "-" + pageable.getPageSize() + "-" +
                (status != null ? status : "null") + "-" + (startDate != null ? startDate : "null") + "-" + (endDate != null ? endDate : "null");
    }

    @Override
    public String buildOrderDetailCacheKey(Integer id, UUID userId) {
        return ORDERS_CACHE_PREFIX + id + "-" + userId;
    }

    @Override
    public String buildOrderItemsCacheKey(Integer orderId, UUID userId, Pageable pageable) {
        return ORDER_ITEMS_CACHE_PREFIX + orderId + "-" + userId + "-" + pageable.getPageNumber() + "-" + pageable.getPageSize();
    }
}