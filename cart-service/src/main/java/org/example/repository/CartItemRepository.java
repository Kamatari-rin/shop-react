package org.example.repository;

import org.example.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Integer> {
    Logger log = LoggerFactory.getLogger(CartItemRepository.class);

    @Query("SELECT * FROM cart_items WHERE cart_id = :cartId")
    Flux<CartItem> findByCartIdInternal(UUID cartId);

    Mono<CartItem> findByCartIdAndProductId(UUID cartId, Integer productId);

    default Flux<CartItem> findByCartId(UUID cartId) {
        log.info("[REPOSITORY] Finding items for cartId: {}", cartId);
        return findByCartIdInternal(cartId)
                .doOnNext(item -> log.debug("[REPOSITORY] Found item for cartId: {}", cartId))
                .doOnError(e -> log.error("[REPOSITORY] Error finding items for cartId: {}", cartId, e));
    }
}
