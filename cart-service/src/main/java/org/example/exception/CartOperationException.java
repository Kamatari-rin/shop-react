package org.example.exception;

public class CartOperationException extends ServiceException {
    public CartOperationException(String message) {
        super(message);
    }
}