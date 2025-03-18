package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.PurchaseResponseDTO;
import org.example.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponseDTO> createPurchase(@RequestHeader("X-User-Id") UUID userId) {
        PurchaseResponseDTO response = purchaseService.createPurchase(userId);
        return ResponseEntity.ok(response);
    }
}