package org.example.exception;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Integer id) {
        super("Product with ID {0} not found", id);
    }
}