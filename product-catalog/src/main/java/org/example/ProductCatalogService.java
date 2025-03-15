package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductCatalogService {
    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogService.class, args);
    }
}