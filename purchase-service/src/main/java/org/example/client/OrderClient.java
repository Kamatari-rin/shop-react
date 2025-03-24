package org.example.client;

import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String orderServiceUrl;

    public OrderClient(RestTemplate restTemplate, @Value("${order.service.url}") String orderServiceUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = orderServiceUrl;
    }

    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        return restTemplate.postForObject(orderServiceUrl, request, OrderDTO.class);
    }
}