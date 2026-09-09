package com.example.ecommerce.exception;

/**
 * Exception thrown when an authenticated user lacks permissions (HTTP 403 Forbidden).
 */
public class AccessDeniedException extends AppException {

    public AccessDeniedException(String message) {
        super(message, 403);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause, 403);
    }
}
