package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.example.dto.DebitBalanceRequestDTO;
import org.example.dto.WalletDTO;
import org.example.service.WalletService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Validated
public class WalletController {
    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    @GetMapping
    public Mono<WalletDTO> getWallet(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Fetching wallet for user: {}", userId);
        return walletService.getOrCreateWallet(userId)
                .doOnSuccess(wallet -> log.debug("Wallet retrieved for user: {}", userId));
    }

    @PutMapping("/debit")
    public Mono<Void> debitBalance(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DebitBalanceRequestDTO request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Debiting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.debitBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance debited for user: {}", userId));
    }

    @PutMapping("/credit")
    public Mono<Void> creditBalance(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DebitBalanceRequestDTO request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Crediting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.creditBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance credited for user: {}", userId));
    }
}