package org.example.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductDetailDTO;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDetailDTO>> getProductById(@PathVariable("id") @NotNull Integer id) {
        log.debug("Fetching product with id: {}", id);
        return productService.getProductById(id)
                .map(productDTO -> {
                    log.debug("Fetched product with id: {}", id);
                    return ResponseEntity.ok(productDTO);
                });
    }
}