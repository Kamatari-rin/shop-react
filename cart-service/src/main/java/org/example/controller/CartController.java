package org.example.controller;

import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<CartDTO> getCart(@RequestHeader("X-User-Id") UUID userId) {
        log.info("Received request to /api/cart");
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CartDTO> addItemToCart(@RequestHeader("X-User-Id") UUID userId,
                                       @Valid @RequestBody CartItemRequestDTO request) {
        return cartService.addItemToCart(userId, request);
    }

    @DeleteMapping("/items/{productId}")
    public Mono<CartDTO> removeItemFromCart(@RequestHeader("X-User-Id") UUID userId,
                                            @PathVariable Integer productId) {
        return cartService.removeItemFromCart(userId, productId);
    }

    @PutMapping("/items")
    public Mono<CartDTO> updateItemQuantity(@RequestHeader("X-User-Id") UUID userId,
                                            @Valid @RequestBody CartItemRequestDTO request) {
        return cartService.updateItemQuantity(userId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> clearCart(@RequestHeader("X-User-Id") UUID userId) {
        return cartService.clearCart(userId);
    }
}