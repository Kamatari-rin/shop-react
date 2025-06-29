package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.CartClient;
import org.example.exception.CartEmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.example.dto.CartDTO;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartClientImpl implements CartClient {
    private static final Logger log = LoggerFactory.getLogger(CartClientImpl.class);
    private final WebClient webClient;
    @Value("${cart.service.url:http://cart-service:8080}")
    private String cartServiceUrl;

    @Override
    public Mono<CartDTO> getCart(UUID userId) {
        if (cartServiceUrl == null || cartServiceUrl.trim().isEmpty()) {
            log.error("cartServiceUrl is not configured");
            return Mono.error(new IllegalStateException("cartServiceUrl is not configured"));
        }
        log.debug("Fetching cart for user: {} with cartServiceUrl: {}", userId, cartServiceUrl);
        return webClient.get()
                .uri(cartServiceUrl).attributes(
                        ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service")
                )
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(CartDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(cart -> log.debug("Successfully fetched cart for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to get cart for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new CartEmptyException(userId.toString());
                })
                .doOnError(e -> log.error("Error fetching cart for user {}: {}", userId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> clearCart(UUID userId) {
        if (cartServiceUrl == null || cartServiceUrl.trim().isEmpty()) {
            log.error("cartServiceUrl is not configured");
            return Mono.error(new IllegalStateException("cartServiceUrl is not configured"));
        }
        log.debug("Clearing cart for user: {} with cartServiceUrl: {}", userId, cartServiceUrl);
        return webClient.delete()
                .uri(cartServiceUrl).attributes(
                        ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service"))
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(v -> log.debug("Successfully cleared cart for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to clear cart for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new RuntimeException("Failed to clear cart", e);
                })
                .doOnError(e -> log.error("Error clearing cart for user {}: {}", userId, e.getMessage(), e));
    }
}