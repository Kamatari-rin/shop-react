package org.example.controller;

import org.example.dto.ApiError;
import org.example.exception.CartOperationException;
import org.example.exception.ProductClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductClientException.class)
    public ResponseEntity<ApiError> handleProductClientException(ProductClientException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Product Service Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(CartOperationException.class)
    public ResponseEntity<ApiError> handleCartOperationException(CartOperationException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Cart Operation Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}