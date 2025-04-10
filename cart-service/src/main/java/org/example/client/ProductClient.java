package org.example.client;

import org.example.dto.ProductDTO;
import org.example.exception.ProductClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ProductClient {
    private final WebClient webClient;
    private final String productDetailUrl;

    public ProductClient(WebClient.Builder webClientBuilder,
                         @Value("${product.detail.url}") String productDetailUrl) {
        this.webClient = webClientBuilder.baseUrl(productDetailUrl).build();
        this.productDetailUrl = productDetailUrl;
    }

    public Mono<ProductDTO> getProductById(Integer productId) {
        return webClient.get()
                .uri("/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .onErrorMap(ex -> new ProductClientException("Failed to fetch product with ID: " + productId, ex));
    }
}