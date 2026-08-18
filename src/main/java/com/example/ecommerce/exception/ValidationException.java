package com.example.ecommerce.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when business or form validation fails.
 */
public class ValidationException extends AppException {

    private final List<String> errorMessages;

    public ValidationException(String message) {
        super(message, 400);
        this.errorMessages = Collections.singletonList(message);
    }

    public ValidationException(List<String> errorMessages) {
        super(String.join(", ", errorMessages), 400);
        this.errorMessages = errorMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
