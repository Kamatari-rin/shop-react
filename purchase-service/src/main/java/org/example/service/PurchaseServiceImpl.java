package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.CartClient;
import org.example.client.OrderClient;
import org.example.client.ProductClient;
import org.example.client.WalletClient;
import org.example.dto.*;
import org.example.enums.PaymentStatus;
import org.example.exception.CartEmptyException;
import org.example.exception.InsufficientBalanceException;
import org.example.exception.PriceMismatchException;
import org.example.exception.WalletNotFoundException;
import org.example.mapper.PurchaseMapper;
import org.example.model.Purchase;
import org.example.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private static final Logger log = LoggerFactory.getLogger(PurchaseServiceImpl.class);
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final WalletClient walletClient;
    private final PurchaseRepository purchaseRepository;
    private final TransactionalOperator transactionalOperator;
    private final PurchaseMapper purchaseMapper;

    @Override
    public Mono<PurchaseResponseDTO> createPurchase(UUID userId) {
        return getValidatedCart(userId)
                .flatMap(cart -> {
                    BigDecimal totalAmount = calculateTotal(cart);
                    return validatePrices(cart)
                            .then(checkAndDebitBalance(userId, totalAmount))
                            .thenReturn(Tuples.of(cart, totalAmount));
                })
                .flatMap(tuple -> {
                    CartDTO cart = tuple.getT1();
                    BigDecimal totalAmount = tuple.getT2();
                    return createOrder(cart)
                            .flatMap(order -> savePurchase(userId, order)
                                    .map(purchase -> Tuples.of(purchase, totalAmount)))
                            .flatMap(purchaseTuple -> clearCart(userId)
                                    .thenReturn(purchaseTuple)
                                    .onErrorResume(e -> rollbackPurchase(userId, purchaseTuple.getT1(), totalAmount)
                                            .then(Mono.error(e))))
                            .onErrorResume(e -> rollbackBalance(userId, totalAmount)
                                    .then(Mono.error(e)));
                })
                .map(tuple -> purchaseMapper.toResponseDto(tuple.getT1(), tuple.getT2()))
                .as(transactionalOperator::transactional)
                .onErrorResume(InsufficientBalanceException.class, e -> {
                    log.error("Insufficient balance for user: {}, error: {}", userId, e.getMessage(), e);
                    return Mono.error(e);
                })
                .onErrorResume(WalletNotFoundException.class, e -> {
                    log.error("Wallet not found for user: {}, error: {}", userId, e.getMessage(), e);
                    return Mono.error(e);
                })
                .onErrorResume(CartEmptyException.class, e -> {
                    log.error("Cart is empty for user: {}, error: {}", userId, e.getMessage(), e);
                    return Mono.error(e);
                })
                .onErrorResume(PriceMismatchException.class, e -> {
                    log.error("Price mismatch for user: {}, error: {}", userId, e.getMessage(), e);
                    return Mono.error(e);
                });
    }

    private Mono<CartDTO> getValidatedCart(UUID userId) {
        log.debug("Fetching cart for user: {}", userId);
        return cartClient.getCart(userId)
                .filter(cart -> !cart.items().isEmpty())
                .switchIfEmpty(Mono.error(new CartEmptyException(userId.toString())));
    }

    private Mono<Void> validatePrices(CartDTO cart) {
        log.debug("Validating prices for cart: {}", cart.id());
        return Flux.fromIterable(cart.items())
                .flatMap(item -> productClient.getProductById(item.productId())
                        .filter(product -> product.price().equals(item.priceAtTime()))
                        .switchIfEmpty(Mono.error(new PriceMismatchException(item.productId()))))
                .then();
    }

    private Mono<Void> checkAndDebitBalance(UUID userId, BigDecimal totalAmount) {
        log.debug("Checking and debiting balance for user: {}, amount: {}", userId, totalAmount);
        return walletClient.getWallet(userId)
                .filter(wallet -> wallet.balance().compareTo(totalAmount) >= 0)
                .switchIfEmpty(Mono.error(new InsufficientBalanceException(userId)))
                .flatMap(wallet -> walletClient.debitBalance(userId, totalAmount));
    }

    private BigDecimal calculateTotal(CartDTO cart) {
        return cart.items().stream()
                .map(item -> item.priceAtTime().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<OrderDetailDTO> createOrder(CartDTO cart) {
        log.debug("Creating order for cart: {}, user: {}", cart.id(), cart.userId());
        CreateOrderRequestDTO request = new CreateOrderRequestDTO(
                cart.userId(),
                cart.items().stream()
                        .map(item -> new CreateOrderRequestDTO.OrderItemRequestDTO(
                                item.productId(),
                                item.quantity()))
                        .toList()
        );
        return orderClient.createOrder(request)
                .retry(2)
                .onErrorMap(WebClientResponseException.class, e -> new RuntimeException("Failed to create order: " + e.getMessage(), e));
    }

    private Mono<Purchase> savePurchase(UUID userId, OrderDetailDTO order) {
        log.debug("Saving purchase for order: {}, user: {}", order.id(), userId);
        Purchase purchase = new Purchase(
                null,
                order.id(),
                userId,
                PaymentStatus.PENDING,
                LocalDateTime.now(),
                "Payment via card"
        );
        return purchaseRepository.save(purchase);
    }

    private Mono<Void> clearCart(UUID userId) {
        log.debug("Clearing cart for user: {}", userId);
        return cartClient.clearCart(userId)
                .retry(2)
                .onErrorMap(e -> new RuntimeException("Failed to clear cart: " + e.getMessage(), e));
    }

    private Mono<Void> rollbackBalance(UUID userId, BigDecimal amount) {
        log.debug("Rolling back balance for user: {}, amount: {}", userId, amount);
        return walletClient.creditBalance(userId, amount)
                .retry(2)
                .onErrorMap(e -> new RuntimeException("Failed to rollback balance: " + e.getMessage(), e));
    }

    private Mono<Void> rollbackPurchase(UUID userId, Purchase purchase, BigDecimal amount) {
        log.debug("Rolling back purchase and balance for user: {}, purchase: {}, amount: {}", userId, purchase.orderId(), amount);
        return purchaseRepository.delete(purchase)
                .then(rollbackBalance(userId, amount))
                .onErrorMap(e -> new RuntimeException("Failed to rollback purchase and balance: " + e.getMessage(), e));
    }
}