package org.example.service;

import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;
import org.example.exception.AlreadyExistsException;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.mapper.UserMapper;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        validateUniqueUsername(userCreateDTO.getUsername(), null);
        validateUniqueEmail(userCreateDTO.getEmail(), null);

        return Optional.of(userCreateDTO)
                .map(userMapper::toEntity)
                .map(userRepository::save)
                .map(userMapper::toDto)
                .get();
    }

    @Override
    public UserDTO getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public UserDTO updateUser(UUID userId, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Optional.ofNullable(userUpdateDTO.getUsername())
                .filter(newUsername -> !newUsername.equals(user.getUsername()))
                .ifPresent(newUsername -> validateUniqueUsername(newUsername, userId));

        Optional.ofNullable(userUpdateDTO.getEmail())
                .filter(newEmail -> !newEmail.equals(user.getEmail()))
                .ifPresent(newEmail -> validateUniqueEmail(newEmail, userId));

        userMapper.updateEntityFromDto(userUpdateDTO, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(UUID userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(
                        user -> userRepository.deleteById(userId),
                        () -> { throw new UserNotFoundException(userId); }
                );
    }

    private void validateUniqueUsername(String username, UUID excludeUserId) {
        userRepository.findByUsername(username)
                .filter(existingUser -> !existingUser.getId().equals(excludeUserId))
                .ifPresent(existingUser -> {
                    throw new AlreadyExistsException("User with username {0} already exists", username);
                });
    }

    private void validateUniqueEmail(String email, UUID excludeUserId) {
        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getId().equals(excludeUserId))
                .ifPresent(existingUser -> {
                    throw new UserAlreadyExistsException(email);
                });
    }
}