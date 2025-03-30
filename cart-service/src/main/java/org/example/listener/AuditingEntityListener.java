package org.example.listener;

import jakarta.persistence.PrePersist;
import org.example.model.Cart;

import java.time.LocalDateTime;

public class AuditingEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Cart cart) {
            cart.setCreatedAt(LocalDateTime.now());
        }
    }
}