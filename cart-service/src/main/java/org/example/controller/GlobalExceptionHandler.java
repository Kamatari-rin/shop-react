package org.example.controller;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<ApiError>> handleDuplicateKey(DuplicateKeyException ex, ServerWebExchange exchange) {
        log.warn("Duplicate key error: {}", ex.getMessage(), ex);
        String message = ex.getMessage().contains("cart_items_pkey")
                ? "Failed to add item to cart: duplicate item ID"
                : "A cart already exists for this user";
        return createErrorResponse(new Exception(message), exchange, HttpStatus.CONFLICT, "Duplicate Key");
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.debug("Validation error: {}", message);
        return createErrorResponse(ex, exchange, HttpStatus.BAD_REQUEST, "Validation Error");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ApiError>> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));
        log.debug("Constraint violation: {}", message);
        return createErrorResponse(ex, exchange, HttpStatus.BAD_REQUEST, "Validation Error");
    }
}