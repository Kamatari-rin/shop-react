package org.example.exception;

public class ProductClientException extends ServiceException {
    public ProductClientException(String message, Throwable cause) {
        super(message, cause);
    }
}