package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.WalletClient;
import org.example.dto.UserCreateDTO;
import org.example.dto.UserDTO;
import org.example.dto.UserUpdateDTO;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final WalletClient walletClient;

    @Override
    public Mono<UserDTO> createUser(UserCreateDTO userCreateDTO) {
        return Mono.zip(
                        validateUniqueUsername(userCreateDTO.getUsername(), null),
                        validateUniqueEmail(userCreateDTO.getEmail(), null)
                )
                .then(Mono.just(userCreateDTO))
                .map(userMapper::toEntity)
                .map(user -> {
                    user.setNew(true);
                    return user;
                })
                .flatMap(userRepository::save)
                .flatMap(user -> walletClient.createWallet(user.getId())
                        .then(userRepository.findById(user.getId())))
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserDTO> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserDTO> updateUser(UUID userId, UserUpdateDTO userUpdateDTO) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> {
                    String newUsername = userUpdateDTO.getUsername();
                    String newEmail = userUpdateDTO.getEmail();
                    Mono<Void> usernameValidation = newUsername != null && !newUsername.equals(user.getUsername())
                            ? validateUniqueUsername(newUsername, userId)
                            : Mono.empty();
                    Mono<Void> emailValidation = newEmail != null && !newEmail.equals(user.getEmail())
                            ? validateUniqueEmail(newEmail, userId)
                            : Mono.empty();
                    return usernameValidation
                            .then(emailValidation)
                            .then(Mono.just(user));
                })
                .map(user -> {
                    userMapper.updateEntityFromDto(userUpdateDTO, user);
                    user.setNew(false);
                    return user;
                })
                .flatMap(userRepository::save)
                .map(userMapper::toDto);
    }

    @Override
    public Mono<Void> deleteUser(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> userRepository.deleteById(userId));
    }

    private Mono<Void> validateUniqueUsername(String username, UUID excludeUserId) {
        return userRepository.findByUsername(username)
                .flatMap(existingUser -> {
                    if (excludeUserId != null && excludeUserId.equals(existingUser.getId())) {
                        return Mono.empty();
                    }
                    return Mono.error(new UserAlreadyExistsException("username", username));
                })
                .then();
    }

    private Mono<Void> validateUniqueEmail(String email, UUID excludeUserId) {
        return userRepository.findByEmail(email)
                .flatMap(existingUser -> {
                    if (excludeUserId != null && excludeUserId.equals(existingUser.getId())) {
                        return Mono.empty();
                    }
                    return Mono.error(new UserAlreadyExistsException("email", email));
                })
                .then();
    }
}