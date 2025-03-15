package org.example.service;

import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;

import java.util.UUID;

public interface CartService {
    CartDTO getCartByUserId(UUID userId);
    CartDTO addItemToCart(UUID userId, CartItemRequestDTO request);
    CartDTO removeItemFromCart(UUID userId, Integer productId);
    CartDTO updateItemQuantity(UUID userId, CartItemRequestDTO request);
    void clearCart(UUID userId);
}