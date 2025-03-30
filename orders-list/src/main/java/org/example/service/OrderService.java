package org.example.service;

import org.example.dto.CreateOrderRequestDTO;
import org.example.dto.OrderDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<OrderDTO> getOrdersByUserId(UUID userId);
    OrderDTO createOrder(CreateOrderRequestDTO request);
}