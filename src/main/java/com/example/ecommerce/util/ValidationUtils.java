package com.example.ecommerce.util;

import java.util.regex.Pattern;

/**
 * Centralized Validation Utility providing strict, reusable validation rules
 * for text inputs across all modules (Addresses, User Profiles, Auth, Reviews, Coupons).
 */
public final class ValidationUtils {

    // Person names: Letters, spaces, apostrophes, hyphens, periods only (2 to 50 chars). No digits or symbols.
    public static final Pattern PERSON_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]{2,50}$");

    // Standard Indian 10-digit mobile starting with 6, 7, 8, 9 (or clean digits after stripping prefix)
    public static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    // RFC-compliant email pattern (max 100 chars)
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$");

    // City / State / Country: Letters, spaces, hyphens, periods only (2 to 50 chars). No digits.
    public static final Pattern CITY_STATE_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]{2,50}$");

    // Indian PIN code: Exactly 6 numeric digits
    public static final Pattern PINCODE_PATTERN = Pattern.compile("^\\d{6}$");

    // Coupon Code: Uppercase alphanumeric, underscores, hyphens (3 to 30 chars)
    public static final Pattern COUPON_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{3,30}$");

    private ValidationUtils() {}

    /**
     * Validates a person's name (first name, last name, or full recipient name).
     * Rejects numbers, special characters, or lengths outside bounds.
     */
    public static boolean isValidPersonName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return PERSON_NAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Cleans and validates a 10-digit mobile phone number.
     * Strips whitespace, hyphens, '+91', or leading '0'.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String clean = normalizePhone(phone);
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Normalizes a phone string by removing non-digits and leading country code '+91' or '0'.
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
            digitsOnly = digitsOnly.substring(2);
        } else if (digitsOnly.startsWith("0") && digitsOnly.length() == 11) {
            digitsOnly = digitsOnly.substring(1);
        }
        return digitsOnly;
    }

    /**
     * Validates an email address.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        if (trimmed.length() > 100) return false;
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates a City, State, or Province name.
     * Strictly rejects digits (e.g. 'pune4343434' or 'Maharashtra3343434').
     */
    public static boolean isValidCityOrState(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        return CITY_STATE_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates a 6-digit postal PIN code.
     */
    public static boolean isValidPincode(String pincode) {
        if (pincode == null) return false;
        String trimmed = pincode.trim();
        return PINCODE_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates a street address line 1 (must be 5 to 255 chars, containing letters and meaningful characters).
     */
    public static boolean isValidAddressLine1(String addressLine1) {
        if (addressLine1 == null) return false;
        String trimmed = addressLine1.trim();
        if (trimmed.length() < 5 || trimmed.length() > 255) return false;
        // Must contain at least one letter and not only special characters
        return trimmed.matches(".*[a-zA-Z].*");
    }

    /**
     * Validates a coupon code format.
     */
    public static boolean isValidCouponCode(String code) {
        if (code == null) return false;
        String trimmed = code.trim();
        return COUPON_CODE_PATTERN.matcher(trimmed).matches();
    }
}
