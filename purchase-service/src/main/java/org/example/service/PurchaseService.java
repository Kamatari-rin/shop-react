package org.example.service;

import org.example.dto.PurchaseResponseDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PurchaseService {
    Mono<PurchaseResponseDTO> createPurchase(UUID userId);
}