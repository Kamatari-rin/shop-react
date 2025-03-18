package org.example.controller;

import org.example.exception.ProductClientException;
import org.example.dto.ApiError;
import org.example.exception.PurchaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PurchaseException.class)
    public ResponseEntity<ApiError> handlePurchaseException(PurchaseException ex, ServletWebRequest request) {
        ApiError error = new ApiError(ex.getMessage(), request.getRequest().getRequestURI(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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