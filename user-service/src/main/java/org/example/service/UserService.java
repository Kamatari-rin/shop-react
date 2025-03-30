package org.example.service;

import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;

import java.util.UUID;

public interface UserService {
    UserDTO createUser(UserCreateDTO userCreateDTO);
    UserDTO getUserById(UUID userId);
    UserDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO);
    void deleteUser(UUID userId);
}