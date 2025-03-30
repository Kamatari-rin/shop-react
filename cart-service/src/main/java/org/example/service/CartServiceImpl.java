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
import org.example.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartByUserId(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartDTO.CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapToCartItemDTO)
                .toList();
        BigDecimal totalAmount = calculateTotalAmount(itemDTOs);
        return cartMapper.toCartDTO(cart, itemDTOs, totalAmount);
    }

    @Override
    @Transactional
    public CartDTO addItemToCart(UUID userId, CartItemRequestDTO request) {
        validateQuantity(request.quantity());

        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            ProductDTO product = productClient.getProductById(request.productId());
            if (product == null) {
                throw new CartOperationException("Product not found with ID: " + request.productId());
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.productId())
                    .quantity(request.quantity())
                    .priceAtTime(product.price())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public CartDTO removeItemFromCart(UUID userId, Integer productId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public CartDTO updateItemQuantity(UUID userId, CartItemRequestDTO request) {
        validateQuantity(request.quantity());

        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new CartOperationException("Item with productId " + request.productId() + " not found in cart"));

        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return getCartByUserId(userId);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO.CartItemDTO mapToCartItemDTO(CartItem item) {
        ProductDTO product = productClient.getProductById(item.getProductId());
        String productName = product != null ? product.name() : "Unknown";
        String imageUrl = product != null ? product.imageUrl() : null;
        return cartMapper.toCartItemDTO(item, productName, imageUrl);
    }

    private BigDecimal calculateTotalAmount(List<CartDTO.CartItemDTO> items) {
        return items.stream()
                .map(item -> item.priceAtTime().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new CartOperationException("Quantity must be greater than 0");
        }
    }
}