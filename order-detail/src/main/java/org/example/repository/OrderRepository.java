package org.example.repository;

import org.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByIdAndUserId(Integer id, UUID userId);
}