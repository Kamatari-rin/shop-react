package org.example.exception;

public class WalletServiceException extends ServiceException {
    public WalletServiceException(String message) {
        super(message);
    }

    public WalletServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}