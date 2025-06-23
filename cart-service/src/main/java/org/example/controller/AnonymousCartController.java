package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart/anonymous")
@RequiredArgsConstructor
@Tag(name = "Anonymous Cart API", description = "API for managing anonymous user carts")
public class AnonymousCartController {
    private static final Logger log = LoggerFactory.getLogger(AnonymousCartController.class);
    private final CartService cartService;

    @Operation(summary = "Get or create anonymous cart", description = "Fetches or creates a cart for an anonymous user by cart ID")
    @ApiResponse(responseCode = "200", description = "Cart found or created",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CartDTO>> getAnonymousCart(@PathVariable UUID id) {
        log.debug("Fetching or creating anonymous cart: {}", id);
        return cartService.getOrCreateAnonymousCart(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Add item to anonymous cart", description = "Adds a product to an anonymous cart by cart ID")
    @ApiResponse(responseCode = "201", description = "Item added",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @PostMapping("/{id}/items")
    public Mono<ResponseEntity<CartDTO>> addItemToAnonymousCart(
            @PathVariable UUID id,
            @Valid @RequestBody CartItemRequestDTO request) {
        log.debug("Adding item to anonymous cart: {}, product: {}", id, request.productId());
        return cartService.addItemToAnonymousCart(id, request)
                .map(cartDTO -> ResponseEntity.status(HttpStatus.CREATED).body(cartDTO));
    }

    @Operation(summary = "Remove item from anonymous cart", description = "Removes a product from an anonymous cart by cart ID")
    @ApiResponse(responseCode = "200", description = "Item removed",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @DeleteMapping("/{id}/items/{productId}")
    public Mono<ResponseEntity<CartDTO>> removeItemFromAnonymousCart(
            @PathVariable UUID id,
            @PathVariable("productId") @NotNull Integer productId) {
        log.debug("Removing item {} from anonymous cart: {}", productId, id);
        return cartService.removeItemFromAnonymousCart(id, productId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Update item quantity in anonymous cart", description = "Updates the quantity of a product in an anonymous cart by cart ID")
    @ApiResponse(responseCode = "200", description = "Quantity updated",
            content = @Content(schema = @Schema(implementation = CartDTO.class)))
    @PutMapping("/{id}/items")
    public Mono<ResponseEntity<CartDTO>> updateItemQuantityInAnonymousCart(
            @PathVariable UUID id,
            @Valid @RequestBody CartItemRequestDTO request) {
        log.debug("Updating item quantity in anonymous cart: {}, product: {}", id, request.productId());
        return cartService.updateItemQuantityInAnonymousCart(id, request)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Clear anonymous cart", description = "Clears all items from an anonymous cart by cart ID")
    @ApiResponse(responseCode = "204", description = "Cart cleared")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> clearAnonymousCart(@PathVariable UUID id) {
        log.debug("Clearing anonymous cart: {}", id);
        return cartService.clearAnonymousCart(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}