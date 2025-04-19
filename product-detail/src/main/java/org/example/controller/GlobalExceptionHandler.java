package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.ApiError;
import org.example.exception.BaseExceptionHandler;
import org.example.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleProductNotFound(ProductNotFoundException ex, ServerWebExchange exchange) {
        log.debug("Product not found: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.NOT_FOUND, "Product Not Found");
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return createErrorResponse(ex, exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}