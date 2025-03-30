package org.example.client;

import org.example.exception.ProductClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.example.dto.ProductDetailDTO;

@Component
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productDetailUrl;

    public ProductClient(RestTemplate restTemplate, @Value("${product.detail.url}") String productDetailUrl) {
        this.restTemplate = restTemplate;
        this.productDetailUrl = productDetailUrl;
    }

    public ProductDetailDTO getProductById(Integer productId) {
        try {
            return restTemplate.getForObject(productDetailUrl + productId, ProductDetailDTO.class);
        } catch (Exception e) {
            throw new ProductClientException("Failed to fetch product with ID: " + productId, e);
        }
    }
}