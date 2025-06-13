package org.example.repository;

import org.example.model.Wallet;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface WalletRepository extends ReactiveCrudRepository<Wallet, Integer> {
    @Query("SELECT * FROM wallets WHERE user_id = :userId FOR UPDATE")
    Mono<Wallet> findByUserIdForUpdate(UUID userId);

    @Modifying
    @Query("UPDATE wallets SET balance = balance - :amount, updated_at = :updatedAt WHERE user_id = :userId AND balance >= :amount")
    Mono<Integer> debitBalance(UUID userId, BigDecimal amount, LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE wallets SET balance = balance + :amount, updated_at = :updatedAt WHERE user_id = :userId")
    Mono<Integer> creditBalance(UUID userId, BigDecimal amount, LocalDateTime updatedAt);
}