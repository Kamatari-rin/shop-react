package org.example.repository;

import org.example.model.Cart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CartRepository extends R2dbcRepository<Cart, Integer> {
    Mono<Cart> findByUserId(UUID userId);
}