package org.example.controller;

import org.example.dto.ApiError;
import org.example.exception.OrderNotFoundException;
import org.example.exception.ProductClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex, ServletWebRequest request) {
        ApiError error = new ApiError(ex.getMessage(), request.getRequest().getRequestURI(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductClientException.class)
    public ResponseEntity<ApiError> handleProductClientException(ProductClientException ex, ServletWebRequest request) {
        ApiError error = new ApiError(ex.getMessage(), request.getRequest().getRequestURI(), HttpStatus.SERVICE_UNAVAILABLE.value());
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, ServletWebRequest request) {
        ApiError error = new ApiError("Internal server error", request.getRequest().getRequestURI(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}