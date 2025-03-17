package org.example.exception;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class NotFoundException extends UserServiceException {
    public NotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public static Supplier<NotFoundException> notFound(String message, Object... args) {
        return () -> new NotFoundException(message, args);
    }
}