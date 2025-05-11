package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.CartClient;
import org.example.client.OrderClient;
import org.example.client.ProductClient;
import org.example.dto.*;
import org.example.enums.PaymentStatus;
import org.example.exception.CartEmptyException;
import org.example.exception.PriceMismatchException;
import org.example.model.Purchase;
import org.example.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {
    private final CartClient cartClient;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final PurchaseRepository purchaseRepository;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<PurchaseResponseDTO> createPurchase(UUID userId) {
        return getValidatedCart(userId)
                .flatMap(cart -> validatePrices(cart).thenReturn(cart))
                .flatMap(this::createOrder)
                .flatMap(order -> savePurchase(userId, order)
                        .map(purchase -> new PurchaseData(purchase, order.totalAmount())))
                .flatMap(data -> clearCart(userId).thenReturn(data))
                .map(this::toResponseDTO)
                .as(transactionalOperator::transactional);
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

    private Mono<OrderDetailDTO> createOrder(CartDTO cart) {
        log.debug("Creating order for cart: {}", cart.id());
        CreateOrderRequestDTO request = new CreateOrderRequestDTO(
                cart.userId(),
                cart.items().stream()
                        .map(item -> new CreateOrderRequestDTO.OrderItemRequestDTO(
                                item.productId(),
                                item.quantity()))
                        .toList()
        );
        return orderClient.createOrder(request);
    }

    private Mono<Purchase> savePurchase(UUID userId, OrderDetailDTO order) {
        log.debug("Saving purchase for order: {}", order.id());
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
        return cartClient.clearCart(userId);
    }

    private PurchaseResponseDTO toResponseDTO(PurchaseData data) {
        return new PurchaseResponseDTO(
                data.purchase().orderId(),
                data.purchase().userId(),
                data.totalAmount(),
                data.purchase().paymentStatus(),
                data.purchase().transactionDate()
        );
    }

    private record PurchaseData(Purchase purchase, BigDecimal totalAmount) {
    }
}