package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import reactor.core.publisher.Hooks;

@EnableCaching
@SpringBootApplication
public class OrderDetailService {
    public static void main(String[] args) {
        SpringApplication.run(OrderDetailService.class, args);
    }
}