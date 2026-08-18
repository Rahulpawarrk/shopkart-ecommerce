package com.example.ecommerce.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely with BCrypt.
 * Never store or compare plain-text passwords.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Hashes a plain-text password using BCrypt with salt.
     *
     * @param plainPassword the plain text password
     * @return salted and hashed BCrypt string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plain-text candidate password against a stored BCrypt hash.
     *
     * @param plainPassword  the candidate password
     * @param hashedPassword the stored BCrypt hash
     * @return true if password matches hash, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            return false;
        }
    }
}
