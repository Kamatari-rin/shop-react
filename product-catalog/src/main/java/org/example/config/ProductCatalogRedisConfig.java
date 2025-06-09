package org.example.config;

import org.example.dto.ProductDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Configuration
public class ProductCatalogRedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, ProductDTO> productRedisTemplate(
            ReactiveRedisConnectionFactory factory, RedisConfig redisConfig) {
        return redisConfig.createReactiveRedisTemplate(factory, redisConfig.objectMapper(), ProductDTO.class);
    }
}
