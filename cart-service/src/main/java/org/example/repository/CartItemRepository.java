package org.example.repository;

import org.example.model.CartItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Integer> {
    Flux<CartItem> findByCartId(Integer cartId);
}