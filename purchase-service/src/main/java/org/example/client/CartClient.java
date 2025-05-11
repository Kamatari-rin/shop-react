package org.example.client;

import org.example.dto.CartDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartClient {
    Mono<CartDTO> getCart(UUID userId);
    Mono<Void> clearCart(UUID userId);
}