package org.example.client.impl;

import lombok.RequiredArgsConstructor;
import org.example.client.ProductClient;
import org.example.dto.ProductDetailDTO;
import org.example.exception.PriceMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ProductClientImpl implements ProductClient {
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(ProductClientImpl.class);

    @Value("${product.detail.url:http://product-detail:8080/api/products}")
    private String productDetailUrl;

    @Override
    public Mono<ProductDetailDTO> getProductById(Integer productId) {
        if (productDetailUrl == null || productDetailUrl.trim().isEmpty()) {
            log.error("productDetailUrl is not configured");
            return Mono.error(new IllegalStateException("productDetailUrl is not configured"));
        }
        log.debug("Fetching product with productDetailUrl: {}, productId: {}", productDetailUrl, productId);

        // Используем полный URL с параметром productId
        String fullUrl = productDetailUrl + "/" + productId;
        return webClient.get()
                .uri(fullUrl) // Прямое указание полного URL
                .retrieve()
                .bodyToMono(ProductDetailDTO.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(product -> log.debug("Successfully fetched product with id: {}", productId))
                .onErrorMap(WebClientResponseException.class, e -> {
                    log.error("Failed to get product {}: status={}, body={}", productId, e.getStatusCode(), e.getResponseBodyAsString());
                    return new PriceMismatchException(productId);
                })
                .doOnError(e -> log.error("Error fetching product {}: {}", productId, e.getMessage(), e));
    }
}