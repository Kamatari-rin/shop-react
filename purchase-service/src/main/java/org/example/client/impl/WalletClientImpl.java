package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.WalletClient;
import org.example.dto.DebitBalanceRequestDTO;
import org.example.dto.WalletDTO;
import org.example.exception.WalletServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletClientImpl implements WalletClient {
    private final WebClient webClient;

    @Value("${wallet.service.url}")
    private String walletServiceUrl;

    @Override
    public Mono<WalletDTO> getWallet(UUID userId) {
        log.debug("Fetching wallet for user: {}", userId);
        return webClient.get()
                .uri(walletServiceUrl + "/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error from wallet-service: status={}, body={}", response.statusCode(), errorBody);
                                    return Mono.error(new WalletServiceException("Wallet service error: " + errorBody));
                                })
                )
                .bodyToMono(WalletDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Failed to fetch wallet: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Void> debitBalance(UUID userId, BigDecimal amount) {
        log.debug("Debiting balance for user: {}, amount: {}", userId, amount);
        return webClient.put()
                .uri(walletServiceUrl + "/{userId}/debit", userId)
                .bodyValue(new DebitBalanceRequestDTO(amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error from wallet-service: status={}, body={}", response.statusCode(), errorBody);
                                    return Mono.error(new WalletServiceException("Wallet service error: " + errorBody));
                                })
                )
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Failed to debit balance: {}", error.getMessage(), error));
    }

    @Override
    public Mono<Void> creditBalance(UUID userId, BigDecimal amount) {
        log.debug("Crediting balance for user: {}, amount: {}", userId, amount);
        return webClient.put()
                .uri(walletServiceUrl + "/{userId}/credit", userId)
                .bodyValue(new DebitBalanceRequestDTO(amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error from wallet-service: status={}, body={}", response.statusCode(), errorBody);
                                    return Mono.error(new WalletServiceException("Wallet service error: " + errorBody));
                                })
                )
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Failed to credit balance: {}", error.getMessage(), error));
    }
}