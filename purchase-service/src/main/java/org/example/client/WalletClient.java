package org.example.client;

import org.example.dto.WalletDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletClient {
    Mono<WalletDTO> getWallet(UUID userId);
    Mono<Void> debitBalance(UUID userId, BigDecimal amount);
    Mono<Void> creditBalance(UUID userId, BigDecimal amount);
}