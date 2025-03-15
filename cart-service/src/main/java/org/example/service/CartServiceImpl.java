package org.example.service;

import org.example.client.ProductClient;
import org.example.dto.CartDTO;
import org.example.dto.CartItemDTO;
import org.example.dto.CartItemRequestDTO;
import org.example.dto.ProductDTO;
import org.example.exception.CartOperationException;
import org.example.mapper.CartMapper;
import org.example.model.Cart;
import org.example.model.CartItem;
import org.example.repository.CartItemRepository;
import org.example.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductClient productClient,
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
        this.cartMapper = cartMapper;
    }

    @Override
    public CartDTO getCartByUserId(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemDTO> itemDTOs = items.stream()
                .map(this::mapToCartItemDTO)
                .toList();
        return cartMapper.toCartDTO(cart, itemDTOs);
    }

    @Override
    public CartDTO addItemToCart(UUID userId, CartItemRequestDTO request) {
        validateQuantity(request.quantity());

        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());

        CartItem item;
        if (existingItem.isPresent()) {
            item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            item = new CartItem();
            item.setCartId(cart.getId());
            item.setProductId(request.productId());
            item.setQuantity(request.quantity());
            ProductDTO product = productClient.getProductById(request.productId());
            item.setPriceAtTime(product != null ? product.price() : null);
        }
        cartItemRepository.save(item);

        return getCartByUserId(userId);
    }

    @Override
    public CartDTO removeItemFromCart(UUID userId, Integer productId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .ifPresent(cartItemRepository::delete);
        return getCartByUserId(userId);
    }

    @Override
    public CartDTO updateItemQuantity(UUID userId, CartItemRequestDTO request) {
        validateQuantity(request.quantity());

        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .orElseThrow(() -> new CartOperationException("Item with productId " + request.productId() + " not found in cart"));
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return getCartByUserId(userId);
    }

    @Override
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    private CartItemDTO mapToCartItemDTO(CartItem item) {
        ProductDTO product = productClient.getProductById(item.getProductId());
        String productName = product != null ? product.name() : "Unknown";
        String imageUrl = product != null ? product.imageUrl() : null;
        return cartMapper.toCartItemDTO(item, productName, imageUrl);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new CartOperationException("Quantity must be greater than 0");
        }
    }
}