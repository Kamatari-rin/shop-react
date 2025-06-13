package org.example.config;

import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderItemListDTO;
import org.example.dto.OrderListDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Configuration
public class OrderRedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, OrderListDTO> orderListRedisTemplate(
            ReactiveRedisConnectionFactory factory, RedisConfig redisConfig) {
        return redisConfig.createReactiveRedisTemplate(factory, OrderListDTO.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, OrderDetailDTO> orderDetailRedisTemplate(
            ReactiveRedisConnectionFactory factory, RedisConfig redisConfig) {
        return redisConfig.createReactiveRedisTemplate(factory, OrderDetailDTO.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, OrderItemListDTO> orderItemListRedisTemplate(
            ReactiveRedisConnectionFactory factory, RedisConfig redisConfig) {
        return redisConfig.createReactiveRedisTemplate(factory, OrderItemListDTO.class);
    }
}