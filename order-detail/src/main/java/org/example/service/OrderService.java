package org.example.service;

import org.example.dto.OrderDetailDTO;

import java.util.UUID;

public interface OrderService {
    OrderDetailDTO getOrderDetail(Integer id, UUID userId);
}