package org.example.client;

import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDetailDTO;
import reactor.core.publisher.Mono;

public interface OrderClient {
    Mono<OrderDetailDTO> createOrder(CreateOrderRequestDTO request);
}