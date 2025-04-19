package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleCartNotFound(CartNotFoundException ex, ServerWebExchange exchange) {
        log.debug("Cart not found: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.NOT_FOUND, "Cart Not Found");
    }

    @ExceptionHandler(ProductClientException.class)
    public Mono<ResponseEntity<ApiError>> handleProductClientException(ProductClientException ex, ServerWebExchange exchange) {
        log.warn("Product service error: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.SERVICE_UNAVAILABLE, "Product Service Error");
    }

    @ExceptionHandler(CartOperationException.class)
    public Mono<ResponseEntity<ApiError>> handleCartOperationException(CartOperationException ex, ServerWebExchange exchange) {
        log.debug("Cart operation error: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.BAD_REQUEST, "Cart Operation Error");
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return createErrorResponse(ex, exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}