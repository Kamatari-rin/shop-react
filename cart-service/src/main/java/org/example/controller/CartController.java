package org.example.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<CartDTO>> getCart(@PathVariable("userId") @NotNull UUID userId) {
        log.debug("Fetching cart for user: {}", userId);
        return cartService.getCartByUserId(userId)
                .map(cartDTO -> {
                    log.debug("Fetched cart for user: {}", userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @PostMapping("/{userId}/items")
    public Mono<ResponseEntity<CartDTO>> addItemToCart(
            @PathVariable("userId") @NotNull UUID userId,
            @Valid @RequestBody CartItemRequestDTO request) {
        log.debug("Adding item to cart for user: {}, product: {}", userId, request.productId());
        return cartService.addItemToCart(userId, request)
                .map(cartDTO -> {
                    log.debug("Added item to cart for user: {}", userId);
                    return ResponseEntity.status(HttpStatus.CREATED).body(cartDTO);
                });
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public Mono<ResponseEntity<CartDTO>> removeItemFromCart(
            @PathVariable("userId") @NotNull UUID userId,
            @PathVariable("productId") @NotNull Integer productId) {
        log.debug("Removing item {} from cart for user: {}", productId, userId);
        return cartService.removeItemFromCart(userId, productId)
                .map(cartDTO -> {
                    log.debug("Removed item {} from cart for user: {}", productId, userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @PutMapping("/{userId}/items")
    public Mono<ResponseEntity<CartDTO>> updateItemQuantity(
            @PathVariable("userId") @NotNull UUID userId,
            @Valid @RequestBody CartItemRequestDTO request) {
        log.debug("Updating item quantity in cart for user: {}, product: {}", userId, request.productId());
        return cartService.updateItemQuantity(userId, request)
                .map(cartDTO -> {
                    log.debug("Updated item quantity in cart for user: {}", userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> clearCart(@PathVariable("userId") @NotNull UUID userId) {
        log.debug("Clearing cart for user: {}", userId);
        return cartService.clearCart(userId)
                .doOnSuccess(v -> log.debug("Cleared cart for user: {}", userId))
                .thenReturn(ResponseEntity.noContent().build());
    }
}