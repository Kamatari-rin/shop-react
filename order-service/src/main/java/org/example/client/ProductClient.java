package org.example.client;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductDetailDTO;
import org.example.exception.ProductClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class ProductClient {
    private final WebClient webClient;
    private final String productDetailUrl;

    public ProductClient(WebClient.Builder webClientBuilder,
                         @Value("${product.detail.url}") String productDetailUrl) {
        this.webClient = webClientBuilder.baseUrl(productDetailUrl).build();
        this.productDetailUrl = productDetailUrl;
    }

    public Flux<ProductDetailDTO> getProductsByIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Flux.error(new ProductClientException("Product IDs list cannot be null or empty", null));
        }
        if (productIds.contains(null)) {
            return Flux.error(new ProductClientException("Product IDs list contains null values", null));
        }
        log.debug("Sending request to {}/api/products with ids: {}", productDetailUrl, productIds);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("ids", productIds).build())
                .retrieve()
                .bodyToFlux(ProductDetailDTO.class)
                .onErrorMap(e -> new ProductClientException("Failed to fetch products with IDs: " + productIds, e));
    }
}