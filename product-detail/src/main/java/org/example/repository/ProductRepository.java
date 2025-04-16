package org.example.repository;

import org.example.model.Product;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends R2dbcRepository<Product, Integer> {

}