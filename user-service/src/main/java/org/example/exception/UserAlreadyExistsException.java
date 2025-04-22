package org.example.exception;

public class UserAlreadyExistsException extends AlreadyExistsException {
    public UserAlreadyExistsException(String field, String value) {
        super("User with {0} {1} already exists", field, value);
    }
}