package org.example.dto;

import java.time.LocalDateTime;

public record ApiError(
        String message,
        String path,
        int status,
        LocalDateTime timestamp
) {
    public ApiError(String message, String path, int status) {
        this(message, path, status, LocalDateTime.now());
    }
}