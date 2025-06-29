package org.example.service;

import org.example.dto.WalletDTO;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {
    Mono<WalletDTO> getOrCreateWallet(UUID userId);
    Mono<Void> debitBalance(UUID userId, BigDecimal amount);
    Mono<Void> creditBalance(UUID userId, BigDecimal amount);
}
