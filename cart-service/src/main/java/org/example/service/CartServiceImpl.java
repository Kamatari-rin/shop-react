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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
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
                .flatMap(this::buildCartDTO);
    }

    @Override
    @Transactional
    public Mono<CartDTO> getOrCreateAnonymousCart(UUID id) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = Cart.builder()
                            .build();
                    return cartRepository.save(newCart);
                }))
                .flatMap(this::buildCartDTO);
    }

    @Override
    @Transactional
    public Mono<CartDTO> addItemToCart(UUID userId, CartItemRequestDTO request) {
        return validateRequest(request)
                .flatMap(req -> getOrCreateCart(userId)
                        .flatMap(cart -> addOrUpdateCartItem(cart, req))
                        .flatMap(this::buildCartDTO));
    }

    @Override
    @Transactional
    public Mono<CartDTO> addItemToAnonymousCart(UUID id, CartItemRequestDTO request) {
        return validateRequest(request)
                .flatMap(req -> cartRepository.findById(id)
                        .switchIfEmpty(Mono.defer(() -> {
                            Cart newCart = Cart.builder()
                                    .build();
                            return cartRepository.save(newCart);
                        }))
                        .flatMap(cart -> addOrUpdateCartItem(cart, req))
                        .flatMap(this::buildCartDTO));
    }

    @Override
    @Transactional
    public Mono<CartDTO> removeItemFromCart(UUID userId, Integer productId) {
        return getOrCreateCart(userId)
                .flatMap(cart -> removeCartItem(cart, productId))
                .flatMap(this::buildCartDTO);
    }

    @Override
    @Transactional
    public Mono<CartDTO> removeItemFromAnonymousCart(UUID id, Integer productId) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.error(new CartNotFoundException(id)))
                .flatMap(cart -> removeCartItem(cart, productId))
                .flatMap(this::buildCartDTO);
    }

    @Override
    @Transactional
    public Mono<CartDTO> updateItemQuantity(UUID userId, CartItemRequestDTO request) {
        return validateRequest(request)
                .flatMap(req -> getOrCreateCart(userId)
                        .flatMap(cart -> updateCartItemQuantity(cart, req))
                        .flatMap(this::buildCartDTO));
    }

    @Override
    @Transactional
    public Mono<CartDTO> updateItemQuantityInAnonymousCart(UUID id, CartItemRequestDTO request) {
        return validateRequest(request)
                .flatMap(req -> cartRepository.findById(id)
                        .switchIfEmpty(Mono.error(new CartNotFoundException(id)))
                        .flatMap(cart -> updateCartItemQuantity(cart, req))
                        .flatMap(this::buildCartDTO));
    }

    @Override
    @Transactional
    public Mono<Void> clearCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new CartNotFoundException(userId)))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItemRepository::delete)
                        .then(cartRepository.delete(cart)));
    }

    @Override
    @Transactional
    public Mono<Void> clearAnonymousCart(UUID id) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.error(new CartNotFoundException(id)))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItemRepository::delete)
                        .then(cartRepository.delete(cart)));
    }

    @Override
    @Transactional
    public Mono<CartDTO> mergeCarts(UUID userId, UUID id) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.error(new CartNotFoundException(id)))
                .flatMap(anonymousCart -> getOrCreateCart(userId)
                        .flatMap(userCart ->
                                cartItemRepository.findByCartId(anonymousCart.getId())
                                        .flatMap(item -> addOrUpdateCartItem(userCart, new CartItemRequestDTO(item.getProductId(), item.getQuantity())))
                                        .collectList()
                                        .then(cartItemRepository.findByCartId(anonymousCart.getId())
                                                .flatMap(cartItemRepository::delete)
                                                .then())
                                        .then(cartRepository.delete(anonymousCart))
                                        .then(Mono.just(userCart))
                        )
                        .flatMap(this::buildCartDTO)
                );
    }


    private Mono<Cart> getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart)
                            .onErrorResume(DuplicateKeyException.class, e -> cartRepository.findByUserId(userId));
                }));
    }


    private Mono<CartItemRequestDTO> validateRequest(CartItemRequestDTO request) {
        return Mono.just(request)
                .filter(req -> req.quantity() > 0)
                .switchIfEmpty(Mono.error(new CartOperationException("Quantity must be greater than 0")));
    }

    private Mono<Cart> addOrUpdateCartItem(Cart cart, CartItemRequestDTO request) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .flatMap(item -> {
                    item.setQuantity(item.getQuantity() + request.quantity());
                    return cartItemRepository.save(item);
                })
                .switchIfEmpty(createNewCartItem(cart.getId(), request))
                .then(Mono.just(cart));
    }

    private Mono<CartItem> createNewCartItem(UUID cartId, CartItemRequestDTO request) {
        return productClient.getProductById(request.productId())
                .switchIfEmpty(Mono.error(new CartOperationException("Product not found with ID: " + request.productId())))
                .flatMap(product -> {
                    CartItem newItem = CartItem.builder()
                            .cartId(cartId)
                            .productId(request.productId())
                            .quantity(request.quantity())
                            .priceAtTime(product.price())
                            .build();
                    return cartItemRepository.save(newItem);
                });
    }

    private Mono<Cart> removeCartItem(Cart cart, Integer productId) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .switchIfEmpty(Mono.error(new CartOperationException("Item with productId " + productId + " not found in cart")))
                .flatMap(cartItemRepository::delete)
                .then(Mono.just(cart));
    }

    private Mono<Cart> updateCartItemQuantity(Cart cart, CartItemRequestDTO request) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .switchIfEmpty(Mono.error(new CartOperationException("Item with productId " + request.productId() + " not found in cart")))
                .flatMap(item -> {
                    item.setQuantity(request.quantity());
                    return cartItemRepository.save(item);
                })
                .then(Mono.just(cart));
    }

    private Mono<CartDTO> buildCartDTO(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId())
                .collectList()
                .map(items -> {
                    cart.setItems(items);
                    return cart;
                })
                .flatMap(c -> Flux.fromIterable(c.getItems())
                        .flatMap(this::mapToCartItemDTO)
                        .collectList()
                        .map(itemDTOs -> {
                            BigDecimal totalAmount = calculateTotalAmount(itemDTOs);
                            return cartMapper.toCartDTO(c, itemDTOs, totalAmount);
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