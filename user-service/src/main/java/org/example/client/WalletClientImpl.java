package org.example.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.WalletDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

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
    public Mono<WalletDTO> createWallet(UUID userId) {
        log.debug("Creating wallet for user: {}", userId);
        return webClient.post()
                .uri(walletServiceUrl + "/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error from wallet-service: status={}, body={}", response.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("Wallet service error: " + errorBody));
                                })
                )
                .bodyToMono(WalletDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Failed to create wallet: {}", error.getMessage(), error));
    }
}