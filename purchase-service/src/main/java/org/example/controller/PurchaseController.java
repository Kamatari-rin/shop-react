package org.example.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PurchaseResponseDTO;
import org.example.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/{userId}")
    public Mono<ResponseEntity<PurchaseResponseDTO>> createPurchase(@PathVariable @NotNull UUID userId) {
        log.debug("Creating purchase for user: {}", userId);
        return purchaseService.createPurchase(userId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("Purchase created for user: {}", userId));
    }
}