package org.example.service;

import org.example.dto.ProductDetailDTO;
import reactor.core.publisher.Mono;

public interface ProductService {
    Mono<ProductDetailDTO> getProductById(Integer id);
}