package org.example.exception;

public class UserAlreadyExistsException extends AlreadyExistsException {
    public UserAlreadyExistsException(String email) {
        super("User with email {0} already exists", email);
    }
}
