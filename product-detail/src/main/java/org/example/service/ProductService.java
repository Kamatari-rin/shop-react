package org.example.service;

import org.example.dto.ProductDetailDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductService {
    Mono<ProductDetailDTO> getProductById(Integer id);
    Flux<ProductDetailDTO> getProductsByIds(List<Integer> ids);
}