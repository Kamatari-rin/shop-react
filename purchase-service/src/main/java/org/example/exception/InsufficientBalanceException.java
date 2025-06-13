package org.example.exception;

import java.util.UUID;

public class InsufficientBalanceException extends ServiceException {
    public InsufficientBalanceException(UUID userId) {
        super("Insufficient balance for user: " + userId);
    }
}