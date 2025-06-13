package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.ProductClient;
import org.example.dto.ProductDetailDTO;
import org.example.exception.PriceMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ProductClientImpl implements ProductClient {
    private final WebClient webClient;

    @Value("${product.detail.url}")
    private String productDetailUrl;

    @Override
    public Mono<ProductDetailDTO> getProductById(Integer productId) {
        return webClient.get()
                .uri(productDetailUrl + "/{productId}", productId)
                .retrieve()
                .bodyToMono(ProductDetailDTO.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.error(new PriceMismatchException(productId)));
    }
}