package org.example.controller;

import org.example.dto.ApiError;
import org.example.exception.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleProductNotFound(ProductNotFoundException ex, ServerWebExchange exchange) {
        ApiError error = new ApiError(ex.getMessage(), exchange.getRequest().getPath().value(), HttpStatus.NOT_FOUND.value());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        ApiError error = new ApiError("Internal server error", exchange.getRequest().getPath().value(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}