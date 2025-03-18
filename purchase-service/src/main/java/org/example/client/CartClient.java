package org.example.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.example.dto.CartDTO;

import java.util.UUID;

@Component
public class CartClient {
    private final RestTemplate restTemplate;
    private final String cartServiceUrl;

    public CartClient(RestTemplate restTemplate, @Value("${cart.service.url}") String cartServiceUrl) {
        this.restTemplate = restTemplate;
        this.cartServiceUrl = cartServiceUrl;
    }

    public CartDTO getCart(UUID userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(cartServiceUrl, HttpMethod.GET, entity, CartDTO.class).getBody();
    }
}