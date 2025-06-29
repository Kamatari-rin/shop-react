package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.OrderClient;
import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderClientImpl implements OrderClient {
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(OrderClientImpl.class);

    @Value("${order.service.url:http://order-service:8080/api/orders}")
    private String orderServiceUrl;

    @Override
    public Mono<OrderDetailDTO> createOrder(CreateOrderRequestDTO request) {
        if (orderServiceUrl == null || orderServiceUrl.trim().isEmpty()) {
            log.error("orderServiceUrl is not configured");
            return Mono.error(new IllegalStateException("orderServiceUrl is not configured"));
        }
        UUID userId = request.userId();
        log.debug("Creating order for user: {} with orderServiceUrl: {}", userId, orderServiceUrl);
        log.debug("Request data: items={}", request.items());

        return webClient.post()
                .uri(orderServiceUrl)
                .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("purchase-service"))
                .header("X-User-Id", userId.toString())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OrderDetailDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.debug("Order created successfully for user: {}", userId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to create order for user {}: status={}, body={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new RuntimeException("Order service error: " + e.getResponseBodyAsString(), e);
                })
                .doOnError(e -> log.error("Error during order creation for user {}: {}", userId, e.getMessage(), e));
    }
}