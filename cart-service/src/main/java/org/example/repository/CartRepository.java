package org.example.repository;

import org.example.model.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CartRepository extends R2dbcRepository<Cart, Integer> {
    Logger log = LoggerFactory.getLogger(CartRepository.class);

    @Query("SELECT * FROM carts WHERE user_id = :userId")
    Mono<Cart> findByUserIdInternal(UUID userId);

    default Mono<Cart> findByUserId(UUID userId) {
        log.info("[REPOSITORY] Finding cart by userId: {}", userId);
        return findByUserIdInternal(userId)
                .doOnError(e -> log.error("[REPOSITORY] Error finding cart for userId: {}", userId, e));
    }
}