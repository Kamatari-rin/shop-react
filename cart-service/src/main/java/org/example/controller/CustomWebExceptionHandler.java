package org.example.controller;

import org.example.dto.ApiError;
import org.example.exception.CartNotFoundException;
import org.example.exception.CartOperationException;
import org.example.exception.ProductClientException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-2)
public class CustomWebExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String error;
        String message;

        if (ex instanceof CartNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            error = "Cart Not Found Error";
            message = ex.getMessage();
        } else if (ex instanceof ProductClientException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            error = "Product Service Error";
            message = ex.getMessage();
        } else if (ex instanceof CartOperationException) {
            status = HttpStatus.BAD_REQUEST;
            error = "Cart Operation Error";
            message = ex.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Internal Server Error";
            message = "An unexpected error occurred: " + ex.getMessage();
            ex.printStackTrace();
        }

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                exchange.getRequest().getPath().toString(),
                exchange.getRequest().getId()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(apiError.toJson().getBytes()))
        );
    }
}