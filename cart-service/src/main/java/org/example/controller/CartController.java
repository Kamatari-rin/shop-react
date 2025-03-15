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

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable UUID userId) {
        CartDTO cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartDTO> addItemToCart(@PathVariable UUID userId,
                                                 @Valid @RequestBody CartItemRequestDTO request) {
        CartDTO updatedCart = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartDTO> removeItemFromCart(@PathVariable UUID userId,
                                                      @PathVariable Integer productId) {
        CartDTO updatedCart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{userId}/items")
    public ResponseEntity<CartDTO> updateItemQuantity(@PathVariable UUID userId,
                                                      @Valid @RequestBody CartItemRequestDTO request) {
        CartDTO updatedCart = cartService.updateItemQuantity(userId, request);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}