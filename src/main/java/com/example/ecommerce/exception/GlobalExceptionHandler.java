package com.example.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Spring MVC Global Exception Handler for centralized error management.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            ResourceNotFoundException.class,
            org.springframework.web.servlet.NoHandlerFoundException.class,
            org.springframework.web.servlet.resource.NoResourceFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, Model model) {
        logger.warn("Page or resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", "The page or resource you are looking for does not exist.");
        return "error/404";
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(ValidationException ex, Model model) {
        logger.warn("Validation failure: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errors", ex.getErrors());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unhandled application exception", ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/500";
    }
}
