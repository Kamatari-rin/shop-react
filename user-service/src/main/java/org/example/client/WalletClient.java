package org.example.client;

import org.example.dto.WalletDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletClient {
    Mono<WalletDTO> createWallet(UUID userId);
}
