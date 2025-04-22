package org.example.service;

import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;
import org.example.model.User;
import org.example.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Mono<UserDTO> createUser(UserCreateDTO userCreateDTO);
    Mono<UserDTO> getUserById(UUID userId);
    Mono<UserDTO> updateUser(UUID userId, UserUpdateDTO userUpdateDTO);
    Mono<Void> deleteUser(UUID userId);
}