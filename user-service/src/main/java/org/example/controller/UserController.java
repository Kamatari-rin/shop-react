package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO userDTO = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @GetMapping
    public ResponseEntity<UserDTO> getUser(@RequestHeader("X-User-Id") UUID userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateUser(@RequestHeader("X-User-Id") UUID userId,
                                              @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserDTO userDTO = userService.updateUser(userId, userUpdateDTO);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestHeader("X-User-Id") UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}