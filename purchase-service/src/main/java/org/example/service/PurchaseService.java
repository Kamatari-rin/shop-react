package org.example.service;

import org.example.dto.PurchaseResponseDTO;

import java.util.UUID;

public interface PurchaseService {
    PurchaseResponseDTO createPurchase(UUID userId);
}