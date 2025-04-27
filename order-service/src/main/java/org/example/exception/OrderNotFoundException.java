package org.example.exception;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(Integer id) {
        super("Order not found with id: ", id);
    }
}