package org.example.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class AuditingEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Auditable auditable) {
            LocalDateTime now = LocalDateTime.now();
            auditable.setCreatedAt(now);
            auditable.setUpdatedAt(now);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            auditable.setUpdatedAt(LocalDateTime.now());
        }
    }
}