package com.example.ecommerce.api.exception;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized REST API Exception Handler.
 * Returns consistent, structured JSON responses for all REST controllers.
 */
@RestControllerAdvice(basePackages = "com.example.ecommerce.api")
public class GlobalRestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "NOT_FOUND"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "VALIDATION_FAILED"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(fe.getField() + ": " + fe.getDefaultMessage());
        }
        String summary = errors.isEmpty() ? "Invalid request payload" : errors.get(0);
        logger.warn("Payload validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(summary, errors, "INVALID_ARGUMENTS"));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArguments(RuntimeException ex) {
        logger.warn("Illegal argument / state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), "BAD_REQUEST"));
    }

    /**
     * Suppress noise for client disconnects / broken pipe exceptions (e.g. user closed tab or refreshed before response finished).
     */
    @ExceptionHandler({
            org.springframework.web.context.request.async.AsyncRequestNotUsableException.class,
            org.apache.catalina.connector.ClientAbortException.class
    })
    public void handleClientAbort(Exception ex) {
        logger.debug("Client aborted/closed connection prematurely: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        if (isClientAbort(ex)) {
            logger.debug("Client aborted/closed connection prematurely: {}", ex.getMessage());
            return null;
        }

        logger.error("Unhandled API error", ex);
        String details = ex.getClass().getSimpleName() + (ex.getMessage() != null ? ": " + ex.getMessage() : "");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Server error: " + details, "INTERNAL_SERVER_ERROR"));
    }

    private boolean isClientAbort(Throwable t) {
        while (t != null) {
            String name = t.getClass().getName();
            String msg = t.getMessage();
            if ("org.apache.catalina.connector.ClientAbortException".equals(name)
                    || "org.springframework.web.context.request.async.AsyncRequestNotUsableException".equals(name)
                    || (t instanceof java.io.IOException && msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset")))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
