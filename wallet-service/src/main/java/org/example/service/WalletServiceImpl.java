package org.example.service;


import lombok.RequiredArgsConstructor;
import org.example.dto.WalletDTO;
import org.example.exception.InsufficientBalanceException;
import org.example.exception.WalletAlreadyExistsException;
import org.example.exception.WalletNotFoundException;
import org.example.mapper.WalletMapper;
import org.example.model.Wallet;
import org.example.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private final WalletRepository walletRepository;
    private final ReactiveRedisTemplate<String, WalletDTO> redisTemplate;
    private final WalletMapper walletMapper;

    private static final String CACHE_KEY_PREFIX = "wallet:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(600);

    @Override
    public Mono<WalletDTO> getWallet(UUID userId) {
        log.debug("Fetching wallet for user: {}", userId);
        String cacheKey = CACHE_KEY_PREFIX + userId;
        return redisTemplate.opsForValue().get(cacheKey)
                .switchIfEmpty(Mono.defer(() -> walletRepository.findByUserIdForUpdate(userId)
                        .map(walletMapper::toDto)
                        .flatMap(dto -> redisTemplate.opsForValue()
                                .set(cacheKey, dto, CACHE_TTL)
                                .thenReturn(dto))))
                .switchIfEmpty(Mono.error(new WalletNotFoundException(userId)));
    }

    @Override
    public Mono<Void> debitBalance(UUID userId, BigDecimal amount) {
        log.debug("Debiting balance for user: {}, amount: {}", userId, amount);
        String cacheKey = CACHE_KEY_PREFIX + userId;
        return walletRepository.findByUserIdForUpdate(userId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(userId)))
                .flatMap(wallet -> {
                    if (wallet.balance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException(userId));
                    }
                    return walletRepository.debitBalance(userId, amount, LocalDateTime.now())
                            .filter(count -> count > 0)
                            .switchIfEmpty(Mono.error(new InsufficientBalanceException(userId)))
                            .then(redisTemplate.delete(cacheKey))
                            .then();
                });
    }

    @Override
    public Mono<Void> creditBalance(UUID userId, BigDecimal amount) {
        log.debug("Crediting balance for user: {}, amount: {}", userId, amount);
        String cacheKey = CACHE_KEY_PREFIX + userId;
        return walletRepository.findByUserIdForUpdate(userId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(userId)))
                .flatMap(wallet -> walletRepository.creditBalance(userId, amount, LocalDateTime.now())
                        .then(redisTemplate.delete(cacheKey))
                        .then());
    }

    @Override
    @Transactional
    public Mono<WalletDTO> createWallet(UUID userId) {
        log.debug("Creating wallet for user: {}", userId);
        String cacheKey = CACHE_KEY_PREFIX + userId;

        return walletRepository.findByUserIdForUpdate(userId)
                .flatMap(wallet -> Mono.<WalletDTO>error(new WalletAlreadyExistsException(userId)))
                .switchIfEmpty(Mono.defer(() ->
                        createNewWallet(userId)
                                .map(walletMapper::toDto)
                                .flatMap(dto -> redisTemplate.opsForValue()
                                        .set(cacheKey, dto, CACHE_TTL)
                                        .thenReturn(dto)
                                )
                ));
    }

    private Mono<Wallet> createNewWallet(UUID userId) {
        Wallet wallet = new Wallet(null, userId, BigDecimal.ZERO, null, null);
        return walletRepository.save(wallet);
    }
}