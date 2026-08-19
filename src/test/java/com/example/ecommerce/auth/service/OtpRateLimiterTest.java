package com.example.ecommerce.auth.service;

import com.example.ecommerce.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OtpRateLimiter Unit Tests")
class OtpRateLimiterTest {

    private final String testEmail = "testlimiter@shopkart.com";

    @BeforeEach
    void setUp() {
        OtpRateLimiter.reset(testEmail, "REGISTRATION");
        OtpRateLimiter.reset(testEmail, "PASSWORD_RESET");
    }

    @Test
    @DisplayName("Should allow up to 3 OTP requests successfully")
    void testAllowUpToThreeRequests() {
        assertDoesNotThrow(() -> OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION"));
        assertDoesNotThrow(() -> OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION"));
        assertDoesNotThrow(() -> OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION"));
    }

    @Test
    @DisplayName("Should block 4th OTP request and enforce 1-hour lockout")
    void testBlockOnFourthAttempt() {
        // Attempt 1, 2, 3
        OtpRateLimiter.checkAndIncrement(testEmail, "PASSWORD_RESET");
        OtpRateLimiter.checkAndIncrement(testEmail, "PASSWORD_RESET");
        OtpRateLimiter.checkAndIncrement(testEmail, "PASSWORD_RESET");

        // Attempt 4 should throw ValidationException
        ValidationException ex = assertThrows(ValidationException.class, () ->
            OtpRateLimiter.checkAndIncrement(testEmail, "PASSWORD_RESET")
        );

        assertTrue(ex.getMessage().contains("exceeded the maximum of 3 OTP requests"));
        assertTrue(ex.getMessage().contains("locked for 1 hour"));
    }

    @Test
    @DisplayName("Should allow requests again after explicit reset upon successful verification")
    void testResetUnlocksIdentifier() {
        // Attempt 1, 2, 3
        OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION");
        OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION");
        OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION");

        // Reset
        OtpRateLimiter.reset(testEmail, "REGISTRATION");

        // Should now be permitted again
        assertDoesNotThrow(() -> OtpRateLimiter.checkAndIncrement(testEmail, "REGISTRATION"));
    }
}
