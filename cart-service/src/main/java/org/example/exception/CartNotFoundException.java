package org.example.exception;

import java.util.UUID;

public class CartNotFoundException extends NotFoundException {
    public CartNotFoundException(UUID userId) {
        super("Cart for user {0} not found", userId);
    }
}