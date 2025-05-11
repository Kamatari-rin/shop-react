package org.example.client;

import org.example.dto.ProductDetailDTO;
import reactor.core.publisher.Mono;

public interface ProductClient {
    Mono<ProductDetailDTO> getProductById(Integer productId);
}
