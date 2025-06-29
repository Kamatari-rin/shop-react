package org.example.repository;

import org.example.model.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, UUID> {
    Logger log = LoggerFactory.getLogger(CartRepository.class);

    @Query("SELECT * FROM carts WHERE user_id = :userId")
    Mono<Cart> findByUserIdInternal(UUID userId);

    @Query("SELECT * FROM carts WHERE user_id = :userId")
    Flux<Cart> findAllByUserId(UUID userId);

    default Mono<Cart> findByUserId(UUID userId) {
        log.info("[REPOSITORY] Finding cart by userId: {}", userId);
        return findByUserIdInternal(userId)
                .doOnError(e -> log.error("[REPOSITORY] Error finding cart for userId: {}", userId, e));
    }

    @Query("DELETE FROM carts WHERE user_id IS NULL AND created_at < :beforeDate")
    Mono<Void> deleteOldAnonymousCarts(LocalDateTime beforeDate);
}