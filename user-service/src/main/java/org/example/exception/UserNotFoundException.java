package org.example.exception;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(UUID userId) {
        super("User with ID {0} not found", userId);
    }
}