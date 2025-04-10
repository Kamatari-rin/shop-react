package org.example.service;

import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartService {
    Mono<CartDTO> getCartByUserId(UUID userId);
    Mono<CartDTO> addItemToCart(UUID userId, CartItemRequestDTO request);
    Mono<CartDTO> removeItemFromCart(UUID userId, Integer productId);
    Mono<CartDTO> updateItemQuantity(UUID userId, CartItemRequestDTO request);
    Mono<Void> clearCart(UUID userId);
}