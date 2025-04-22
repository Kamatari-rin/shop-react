package org.example.exception;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class AlreadyExistsException extends ServiceException {
    public AlreadyExistsException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public static Supplier<AlreadyExistsException> alreadyExists(String message, Object... args) {
        return () -> new AlreadyExistsException(message, args);
    }
}