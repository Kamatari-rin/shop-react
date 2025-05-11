package org.example.exception;

public class PriceMismatchException extends ServiceException {
    public PriceMismatchException(Integer productId) {
        super("Price mismatch for product ID: " + productId);
    }
}