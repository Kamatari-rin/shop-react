package org.example.config;

import org.example.dto.ProductDetailDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Configuration
public class ProductDetailRedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, ProductDetailDTO> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory, RedisConfig redisConfig) {
        return redisConfig.createReactiveRedisTemplate(factory, redisConfig.objectMapper(), ProductDetailDTO.class);
    }
}