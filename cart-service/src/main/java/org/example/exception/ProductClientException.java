package org.example.exception;

public class ProductClientException extends RuntimeException {
    public ProductClientException(String message, Throwable cause) {
        super(message, cause);
    }
}