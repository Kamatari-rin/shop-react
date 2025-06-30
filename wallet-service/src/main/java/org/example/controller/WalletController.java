package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.DebitBalanceRequestDTO;
import org.example.dto.WalletDTO;
import org.example.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    private UUID resolveUserId(@AuthenticationPrincipal Jwt jwt,
                               @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        if (jwt == null) {
            log.error("No JWT provided in request");
            throw new SecurityException("JWT is required for authentication");
        }

        String azp = jwt.getClaimAsString("azp");
        if ("purchase-service".equals(azp)) {
            if (xUserId == null) {
                log.error("No X-User-Id provided for purchase-service request");
                throw new SecurityException("X-User-Id is required for purchase-service authentication");
            }
            try {
                return UUID.fromString(xUserId);
            } catch (IllegalArgumentException e) {
                log.error("Invalid X-User-Id format: {}", xUserId, e);
                throw new IllegalArgumentException("Invalid user ID format in X-User-Id header");
            }
        } else if ("frontend-client".equals(azp)) {
            try {
                return UUID.fromString(jwt.getSubject());
            } catch (IllegalArgumentException e) {
                log.error("Invalid user ID in JWT subject: {}", jwt.getSubject(), e);
                throw new IllegalArgumentException("Invalid user ID format in JWT subject");
            }
        } else {
            log.error("Unsupported azp claim: {}", azp);
            throw new SecurityException("Unsupported client: " + azp);
        }
    }

    @GetMapping
    public Mono<WalletDTO> getWallet(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId) {
        UUID userId = resolveUserId(jwt, xUserId);
        log.debug("Fetching wallet for user: {}", userId);
        return walletService.getOrCreateWallet(userId)
                .doOnSuccess(wallet -> log.debug("Wallet retrieved for user: {}", userId))
                .onErrorMap(e -> {
                    log.error("Error fetching wallet for user {}: {}", userId, e.getMessage(), e);
                    return e instanceof RuntimeException ? e : new RuntimeException("Failed to fetch wallet", e);
                });
    }

    @PutMapping("/debit")
    public Mono<Void> debitBalance(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @Valid @RequestBody DebitBalanceRequestDTO request) {
        UUID userId = resolveUserId(jwt, xUserId);
        log.debug("Debiting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.debitBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance debited for user: {}", userId))
                .onErrorMap(e -> {
                    log.error("Error debiting balance for user {}: {}", userId, e.getMessage(), e);
                    return e instanceof RuntimeException ? e : new RuntimeException("Failed to debit balance", e);
                });
    }

    @PutMapping("/credit")
    public Mono<Void> creditBalance(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @Valid @RequestBody DebitBalanceRequestDTO request) {
        UUID userId = resolveUserId(jwt, xUserId);
        log.debug("Crediting balance for user: {}, amount: {}", userId, request.amount());
        return walletService.creditBalance(userId, request.amount())
                .doOnSuccess(v -> log.debug("Balance credited for user: {}", userId))
                .onErrorMap(e -> {
                    log.error("Error crediting balance for user {}: {}", userId, e.getMessage(), e);
                    return e instanceof RuntimeException ? e : new RuntimeException("Failed to credit balance", e);
                });
    }
}