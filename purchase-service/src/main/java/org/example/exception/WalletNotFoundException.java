package org.example.exception;

import java.util.UUID;

public class WalletNotFoundException extends NotFoundException {
    public WalletNotFoundException(UUID userId) {
        super("Wallet with user ID {0} not found", userId);
    }
}