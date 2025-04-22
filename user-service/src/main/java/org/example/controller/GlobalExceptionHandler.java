package org.example.controller;

import jakarta.validation.ConstraintViolationException;
import org.example.exception.ApiError;
import org.example.exception.BaseExceptionHandler;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleUserNotFound(UserNotFoundException ex, ServerWebExchange exchange) {
        log.debug("User not found: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.NOT_FOUND, "User Not Found");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ApiError>> handleUserAlreadyExists(UserAlreadyExistsException ex, ServerWebExchange exchange) {
        log.debug("User already exists: {}", ex.getMessage());
        return createErrorResponse(ex, exchange, HttpStatus.CONFLICT, "User Already Exists");
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

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return createErrorResponse(ex, exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
    }
}