package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.ProductClient;
import org.example.dto.CartDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.dto.ProductDTO;
import org.example.exception.CartNotFoundException;
import org.example.exception.CartOperationException;
import org.example.mapper.CartMapper;
import org.example.model.Cart;
import org.example.model.CartItem;
import org.example.repository.CartItemRepository;
import org.example.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public Mono<CartDTO> getCartByUserId(UUID userId) {
        return getOrCreateCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .collectList()
                        .map(items -> {
                            cart.setItems(items);
                            return cart;
                        }))
                .flatMap(cart -> Flux.fromIterable(cart.getItems())
                        .flatMap(this::mapToCartItemDTO)
                        .collectList()
                        .map(itemDTOs -> {
                            BigDecimal totalAmount = calculateTotalAmount(itemDTOs);
                            return cartMapper.toCartDTO(cart, itemDTOs, totalAmount);
                        }));
    }

    @Override
    @Transactional
    public Mono<CartDTO> addItemToCart(UUID userId, CartItemRequestDTO request) {
        return Mono.just(request)
                .filter(req -> req.quantity() > 0)
                .switchIfEmpty(Mono.error(new CartOperationException("Quantity must be greater than 0")))
                .flatMap(req -> getOrCreateCart(userId)
                        .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                .filter(item -> item.getProductId().equals(req.productId()))
                                .next()
                                .flatMap(item -> {
                                    item.setQuantity(item.getQuantity() + req.quantity());
                                    return cartItemRepository.save(item);
                                })
                                .switchIfEmpty(productClient.getProductById(req.productId())
                                        .switchIfEmpty(Mono.error(new CartOperationException("Product not found with ID: " + req.productId())))
                                        .flatMap(product -> {
                                            CartItem newItem = CartItem.builder()
                                                    .cartId(cart.getId())
                                                    .productId(req.productId())
                                                    .quantity(req.quantity())
                                                    .priceAtTime(product.price())
                                                    .build();
                                            return cartItemRepository.save(newItem);
                                        }))
                                .then(Mono.defer(() -> getCartByUserId(userId)))));
    }

    @Override
    @Transactional
    public Mono<CartDTO> removeItemFromCart(UUID userId, Integer productId) {
        return getOrCreateCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .filter(item -> item.getProductId().equals(productId))
                        .next()
                        .switchIfEmpty(Mono.error(new CartOperationException("Item with productId " + productId + " not found in cart")))
                        .flatMap(cartItemRepository::delete)
                        .then(Mono.defer(() -> getCartByUserId(userId))));
    }

    @Override
    @Transactional
    public Mono<CartDTO> updateItemQuantity(UUID userId, CartItemRequestDTO request) {
        return Mono.just(request)
                .filter(req -> req.quantity() > 0)
                .switchIfEmpty(Mono.error(new CartOperationException("Quantity must be greater than 0")))
                .flatMap(req -> getOrCreateCart(userId)
                        .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                .filter(item -> item.getProductId().equals(req.productId()))
                                .next()
                                .switchIfEmpty(Mono.error(new CartOperationException("Item with productId " + req.productId() + " not found in cart")))
                                .flatMap(item -> {
                                    item.setQuantity(req.quantity());
                                    return cartItemRepository.save(item);
                                })
                                .then(Mono.defer(() -> getCartByUserId(userId)))));
    }

    @Override
    @Transactional
    public Mono<Void> clearCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new CartNotFoundException(userId)))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItemRepository::delete)
                        .then(Mono.empty()));
    }

    private Mono<Cart> getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                }));
    }

    private Mono<CartDTO.CartItemDTO> mapToCartItemDTO(CartItem item) {
        return productClient.getProductById(item.getProductId())
                .defaultIfEmpty(new ProductDTO(item.getProductId(), "Unknown", item.getPriceAtTime(), null))
                .map(product -> cartMapper.toCartItemDTO(item, product.name(), product.imageUrl()));
    }

    private BigDecimal calculateTotalAmount(List<CartDTO.CartItemDTO> items) {
        return items.stream()
                .filter(item -> item.priceAtTime() != null)
                .map(item -> item.priceAtTime().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}