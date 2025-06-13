package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/{userId}")
    public Mono<WalletDTO> getWallet(@PathVariable UUID userId) {
        log.debug("Fetching wallet for user: {}", userId);
        return walletService.getWallet(userId)
                .doOnSuccess(wallet -> log.debug("Wallet retrieved for user: {}", userId));
    }

    @PutMapping("/{userId}/debit")
    public Mono<Void> debitBalance(@PathVariable UUID userId, @Valid @RequestBody DebitBalanceRequestDTO request) {
        log.debug("Debiting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.debitBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance debited for user: {}", userId));
    }

    @PutMapping("/{userId}/credit")
    public Mono<Void> creditBalance(@PathVariable UUID userId, @Valid @RequestBody DebitBalanceRequestDTO request) {
        log.debug("Crediting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.creditBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance credited for user: {}", userId));
    }

    @PostMapping("/{userId}")
    public Mono<WalletDTO> createWallet(@PathVariable UUID userId) {
        log.debug("Creating wallet for user: {}", userId);
        return walletService.createWallet(userId)
                .doOnSuccess(wallet -> log.debug("Wallet created for user: {}", userId));
    }
}