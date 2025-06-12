package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @Operation(summary = "Get cart by user ID", description = "Fetches the cart for a given user")
    @ApiResponse(responseCode = "200", description = "Cart found",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<CartDTO>> getCart(
            @Parameter(description = "User ID") @PathVariable("userId") @NotNull UUID userId) {
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
    @PostMapping("/{userId}/items")
    public Mono<ResponseEntity<CartDTO>> addItemToCart(
            @Parameter(description = "User ID") @PathVariable("userId") @NotNull UUID userId,
            @Valid @RequestBody CartItemRequestDTO request) {
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
    @DeleteMapping("/{userId}/items/{productId}")
    public Mono<ResponseEntity<CartDTO>> removeItemFromCart(
            @Parameter(description = "User ID") @PathVariable("userId") @NotNull UUID userId,
            @Parameter(description = "Product ID") @PathVariable("productId") @NotNull Integer productId) {
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
    @PutMapping("/{userId}/items")
    public Mono<ResponseEntity<CartDTO>> updateItemQuantity(
            @Parameter(description = "User ID") @PathVariable("userId") @NotNull UUID userId,
            @Valid @RequestBody CartItemRequestDTO request) {
        log.debug("Updating item quantity in cart for user: {}, product: {}", userId, request.productId());
        return cartService.updateItemQuantity(userId, request)
                .map(cartDTO -> {
                    log.debug("Updated item quantity in cart for user: {}", userId);
                    return ResponseEntity.ok(cartDTO);
                });
    }

    @Operation(summary = "Clear cart", description = "Clears all items from the user's cart")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> clearCart(
            @Parameter(description = "User ID") @PathVariable("userId") @NotNull UUID userId) {
        log.debug("Clearing cart for user: {}", userId);
        return cartService.clearCart(userId)
                .doOnSuccess(v -> log.debug("Cleared cart for user: {}", userId))
                .thenReturn(ResponseEntity.noContent().build());
    }
}