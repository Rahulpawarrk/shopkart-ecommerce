package com.example.ecommerce.exception;

/**
 * Exception thrown when a requested entity cannot be found.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
