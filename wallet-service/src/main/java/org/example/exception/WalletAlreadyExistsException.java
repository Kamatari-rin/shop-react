package org.example.exception;

import java.util.UUID;

public class WalletAlreadyExistsException extends AlreadyExistsException {
    public WalletAlreadyExistsException(UUID userId) {
        super("Wallet already exists for user ID {0}", userId);
    }
}