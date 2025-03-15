package org.example.controller;

import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@RequestHeader("X-User-Id") UUID userId) {
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@RequestHeader("X-User-Id") UUID userId,
                                                 @Valid @RequestBody CartItemRequestDTO request) {
        CartDTO updatedCart = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDTO> removeItemFromCart(@RequestHeader("X-User-Id") UUID userId,
                                                      @PathVariable Integer productId) {
        CartDTO updatedCart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/items")
    public ResponseEntity<CartDTO> updateItemQuantity(@RequestHeader("X-User-Id") UUID userId,
                                                      @Valid @RequestBody CartItemRequestDTO request) {
        CartDTO updatedCart = cartService.updateItemQuantity(userId, request);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}