package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.WalletClient;
import org.example.dto.DebitBalanceRequestDTO;
import org.example.dto.WalletDTO;
import org.example.exception.WalletServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletClientImpl implements WalletClient {
    private static final Logger log = LoggerFactory.getLogger(WalletClientImpl.class);
    private final WebClient webClient;

    @Value("${wallet.service.url}")
    private String walletServiceUrl;

    @Override
    public Mono<WalletDTO> getWallet(UUID userId) {
        if (walletServiceUrl == null || walletServiceUrl.trim().isEmpty()) {
            log.error("walletServiceUrl is not configured");
            return Mono.error(new IllegalStateException("walletServiceUrl is not configured"));
        }
        log.debug("Fetching wallet for user: {} with walletServiceUrl: {}", userId, walletServiceUrl);
        return webClient.get()
                .uri(walletServiceUrl + "/api/wallets")
                .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service"))
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(wallet -> log.debug("Successfully fetched wallet for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to get wallet for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new WalletServiceException("Failed to get wallet for user: " + userId, e);
                })
                .doOnError(e -> log.error("Error fetching wallet for user {}: {}", userId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> debitBalance(UUID userId, BigDecimal amount) {
        if (walletServiceUrl == null || walletServiceUrl.trim().isEmpty()) {
            log.error("walletServiceUrl is not configured");
            return Mono.error(new IllegalStateException("walletServiceUrl is not configured"));
        }
        log.debug("Debiting balance for user: {} with amount: {}", userId, amount);
        return webClient.put()
                .uri(walletServiceUrl + "/api/wallets/debit")
                .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service"))
                .header("X-User-Id", userId.toString())
                .bodyValue(new DebitBalanceRequestDTO(amount))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(v -> log.debug("Successfully debited balance for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to debit balance for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new WalletServiceException("Failed to debit balance for user: " + userId, e);
                })
                .doOnError(e -> log.error("Error debiting balance for user {}: {}", userId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> creditBalance(UUID userId, BigDecimal amount) {
        if (walletServiceUrl == null || walletServiceUrl.trim().isEmpty()) {
            log.error("walletServiceUrl is not configured");
            return Mono.error(new IllegalStateException("walletServiceUrl is not configured"));
        }
        log.debug("Crediting balance for user: {} with amount: {}", userId, amount);
        return webClient.put()
                .uri(walletServiceUrl + "/api/wallets/credit")
                .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service"))
                .header("X-User-Id", userId.toString())
                .bodyValue(new DebitBalanceRequestDTO(amount))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(v -> log.debug("Successfully credited balance for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to credit balance for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new WalletServiceException("Failed to credit balance for user: " + userId, e);
                })
                .doOnError(e -> log.error("Error crediting balance for user {}: {}", userId, e.getMessage(), e));
    }
}