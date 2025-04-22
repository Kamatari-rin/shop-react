package org.example.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
    @Nullable
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Nullable
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Nullable
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;

    @Nullable
    @Size(max = 20, message = "Role must not exceed 20 characters")
    private String role;
}