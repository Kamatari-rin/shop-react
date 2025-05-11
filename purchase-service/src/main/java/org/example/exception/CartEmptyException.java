package org.example.exception;

public class CartEmptyException extends ServiceException {
    public CartEmptyException(String userId) {
        super("Cart is empty for user: " + userId);
    }
}