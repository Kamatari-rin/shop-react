package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "API for managing user carts")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    @Operation(summary = "Get cart", description = "Fetches the cart for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Cart found",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @GetMapping
    public Mono<ResponseEntity<CartDTO>> getCart(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Fetching cart for user: {}", userId);
        return cartService.getCartByUserId(userId)
                .map(cartDTO -> {
                    log.debug("Fetched cart for user: {}", userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @Operation(summary = "Add item to cart", description = "Adds a product to the user's cart")
    @ApiResponse(responseCode = "201", description = "Item added",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @PostMapping("/items")
    public Mono<ResponseEntity<CartDTO>> addItemToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequestDTO request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Adding item to cart for user: {}, product: {}", userId, request.productId());
        return cartService.addItemToCart(userId, request)
                .map(cartDTO -> {
                    log.debug("Added item to cart for user: {}", userId);
                    return ResponseEntity.status(HttpStatus.CREATED).body(cartDTO);
                });
    }

    @Operation(summary = "Remove item from cart", description = "Removes a product from the user's cart")
    @ApiResponse(responseCode = "200", description = "Item removed",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @DeleteMapping("/items/{productId}")
    public Mono<ResponseEntity<CartDTO>> removeItemFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("productId") @NotNull Integer productId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Removing item {} from cart for user: {}", productId, userId);
        return cartService.removeItemFromCart(userId, productId)
                .map(cartDTO -> {
                    log.debug("Removed item {} from cart for user: {}", productId, userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @Operation(summary = "Update item quantity", description = "Updates the quantity of a product in the user's cart")
    @ApiResponse(responseCode = "200", description = "Quantity updated",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @PutMapping("/items")
    public Mono<ResponseEntity<CartDTO>> updateItemQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequestDTO request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Updating item quantity in cart for user: {}, product: {}", userId, request.productId());
        return cartService.updateItemQuantity(userId, request)
                .map(cartDTO -> {
                    log.debug("Updated item quantity in cart for user: {}", userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @Operation(summary = "Clear cart", description = "Clears all items from the user's cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    @DeleteMapping
    public Mono<ResponseEntity<Void>> clearCart(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Clearing cart for user: {}", userId);
        return cartService.clearCart(userId)
                .doOnSuccess(v -> log.debug("Cleared cart for user: {}", userId))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Merge anonymous cart with authenticated user's cart", description = "Merges an anonymous cart into the authenticated user's cart")
    @ApiResponse(responseCode = "200", description = "Carts merged",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @PostMapping("/merge/{id}")
    public Mono<ResponseEntity<CartDTO>> mergeCarts(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Merging anonymous cart {} with user cart: {}", id, userId);
        return cartService.mergeCarts(userId, id)
                .map(ResponseEntity::ok);
    }
}