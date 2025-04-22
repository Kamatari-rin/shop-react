package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public abstract class BaseExceptionHandler {

    protected Mono<ResponseEntity<ApiError>> createErrorResponse(
            Throwable ex, ServerWebExchange exchange, HttpStatus status, String error) {
        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                error,
                ex.getMessage() != null ? ex.getMessage() : "No details available",
                exchange.getRequest().getPath().value(),
                exchange.getRequest().getId()
        );
        return Mono.just(ResponseEntity.status(status).body(apiError));
    }

    @ExceptionHandler(NotFoundException.class)
    protected Mono<ResponseEntity<ApiError>> handleNotFound(NotFoundException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, exchange, HttpStatus.NOT_FOUND, "Not Found");
    }

    @ExceptionHandler(AlreadyExistsException.class)
    protected Mono<ResponseEntity<ApiError>> handleAlreadyExists(AlreadyExistsException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, exchange, HttpStatus.CONFLICT, "Already Exists");
    }

    @ExceptionHandler(ServiceException.class)
    protected Mono<ResponseEntity<ApiError>> handleServiceException(ServiceException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Service Error");
    }
}