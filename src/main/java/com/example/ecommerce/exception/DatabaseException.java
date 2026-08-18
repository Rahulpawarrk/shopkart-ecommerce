package com.example.ecommerce.exception;

/**
 * Exception thrown when a database operation fails in the DAO layer.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message, 500);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}
