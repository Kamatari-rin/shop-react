package org.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String username,
        String email,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}