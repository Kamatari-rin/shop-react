package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.OrderDTO;
import org.example.mapper.OrderMapper;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<OrderDTO> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toDto)
                .toList();
    }
}