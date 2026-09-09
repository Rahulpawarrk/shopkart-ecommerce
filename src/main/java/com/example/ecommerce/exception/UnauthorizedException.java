package com.example.ecommerce.exception;

/**
 * Exception thrown when a user is unauthenticated or session has expired.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message, 401);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, 401);
    }
}
