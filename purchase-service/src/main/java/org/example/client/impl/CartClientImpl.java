package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.CartClient;
import org.example.exception.CartEmptyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.example.dto.CartDTO;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartClientImpl implements CartClient {
    private final WebClient webClient;

    @Value("${cart.service.url}")
    private String cartServiceUrl;

    @Override
    public Mono<CartDTO> getCart(UUID userId) {
        return webClient.get()
                .uri(cartServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(CartDTO.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.error(new CartEmptyException(userId.toString())));
    }

    @Override
    public Mono<Void> clearCart(UUID userId) {
        return webClient.delete()
                .uri(cartServiceUrl + "/{userId}", userId)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5));
    }
}