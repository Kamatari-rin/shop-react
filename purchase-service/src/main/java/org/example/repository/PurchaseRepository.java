package org.example.repository;

import org.example.enums.PaymentStatus;
import org.example.model.Purchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PurchaseRepository extends ReactiveCrudRepository<Purchase, Integer> {

    Flux<Purchase> findByUserId(UUID userId, Pageable pageable);

    Mono<Long> countByUserId(UUID userId);

    Flux<Purchase> findByOrderId(Integer orderId);

    Flux<Purchase> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    Mono<Long> countByPaymentStatus(PaymentStatus paymentStatus);
}