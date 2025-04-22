package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public Mono<ResponseEntity<UserDTO>> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        log.debug("Creating user with username: {}", userCreateDTO.getUsername());
        return userService.createUser(userCreateDTO)
                .map(userDTO -> {
                    log.debug("Created user with id: {}", userDTO.id());
                    return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> getUser(@PathVariable("id") @NotNull UUID userId) {
        log.debug("Fetching user with id: {}", userId);
        return userService.getUserById(userId)
                .map(userDTO -> {
                    log.debug("Fetched user with id: {}", userId);
                    return ResponseEntity.ok(userDTO);
                });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> updateUser(
            @PathVariable("id") @NotNull UUID userId,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        log.debug("Updating user with id: {}", userId);
        return userService.updateUser(userId, userUpdateDTO)
                .map(userDTO -> {
                    log.debug("Updated user with id: {}", userId);
                    return ResponseEntity.ok(userDTO);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") @NotNull UUID userId) {
        log.debug("Deleting user with id: {}", userId);
        return userService.deleteUser(userId)
                .doOnSuccess(v -> log.debug("Deleted user with id: {}", userId))
                .thenReturn(ResponseEntity.noContent().build());
    }
}