package org.example.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ProductDetailDTO;
import org.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @GetMapping
    public Mono<ResponseEntity<List<ProductDetailDTO>>> getProductsByIds(@RequestParam("ids") @NotEmpty List<Integer> ids) {
        log.debug("Fetching products with ids: {}", ids);
        return productService.getProductsByIds(ids)
                .collectList()
                .map(products -> {
                    log.debug("Fetched {} products", products.size());
                    return ResponseEntity.ok(products);
                });
    }
}