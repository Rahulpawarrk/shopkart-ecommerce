package com.example.ecommerce.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil BCrypt Hashing & Verification Tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Should generate valid BCrypt hash for non-empty password")
    void testHashPasswordSuccess() {
        String plainPassword = "SecretPassword@2026";
        String hash = PasswordUtil.hashPassword(plainPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(PasswordUtil.checkPassword(plainPassword, hash));
    }

    @Test
    @DisplayName("Should reject incorrect plain password against valid hash")
    void testCheckPasswordMismatch() {
        String plainPassword = "CorrectPassword123";
        String wrongPassword = "WrongPassword123";
        String hash = PasswordUtil.hashPassword(plainPassword);

        assertFalse(PasswordUtil.checkPassword(wrongPassword, hash));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when hashing null or empty password")
    void testHashNullOrEmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
    }

    @Test
    @DisplayName("Should safely return false for null inputs during checkPassword")
    void testCheckPasswordWithNulls() {
        assertFalse(PasswordUtil.checkPassword(null, "$2a$12$somehash"));
        assertFalse(PasswordUtil.checkPassword("password", null));
        assertFalse(PasswordUtil.checkPassword(null, null));
    }
}
