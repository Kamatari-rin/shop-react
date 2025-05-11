package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.client.OrderClient;
import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDetailDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClientImpl implements OrderClient {
    private final WebClient webClient;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Override
    public Mono<OrderDetailDTO> createOrder(CreateOrderRequestDTO request) {
        String uri = orderServiceUrl + "/{userId}";
        log.debug("Sending request to order-service: uri={}, userId={}, items={}", uri, request.userId(), request.items());
        return webClient.post()
                .uri(uri, request.userId())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error from order-service: status={}, body={}", response.statusCode(), errorBody);
                                    return Mono.<Throwable>error(new RuntimeException("Order service error: " + errorBody));
                                })
                )
                .bodyToMono(OrderDetailDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnError(error -> log.error("Failed to create order: {}", error.getMessage(), error));
    }
}
