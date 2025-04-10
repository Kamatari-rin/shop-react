package org.example.controller;

import org.example.dto.ApiError;
import org.example.exception.CartNotFoundException;
import org.example.exception.CartOperationException;
import org.example.exception.ProductClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public Mono<ApiError> handleCartNotFound(CartNotFoundException ex, ServerRequest request) {
        return Mono.just(ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Cart Not Found Error",
                ex.getMessage(),
                request.path()
        ));
    }

    @ExceptionHandler(ProductClientException.class)
    public Mono<ApiError> handleProductClientException(ProductClientException ex, ServerRequest request) {
        return Mono.just(ApiError.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Product Service Error",
                ex.getMessage(),
                request.path()
        ));
    }

    @ExceptionHandler(CartOperationException.class)
    public Mono<ApiError> handleCartOperationException(CartOperationException ex, ServerRequest request) {
        return Mono.just(ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Cart Operation Error",
                ex.getMessage(),
                request.path()
        ));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ApiError> handleGenericException(Exception ex, ServerRequest request) {
        return Mono.just(ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.path()
        ));
    }
}