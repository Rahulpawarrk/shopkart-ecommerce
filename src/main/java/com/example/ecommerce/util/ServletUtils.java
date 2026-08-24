package com.example.ecommerce.util;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * Utility methods for safe HTTP request parameter parsing.
 * Prevents NumberFormatException crashes (DoS via malformed input).
 */
public final class ServletUtils {

    private ServletUtils() {}

    public static int parseIntParam(HttpServletRequest req, String name, int fallback) {
        try {
            String val = req.getParameter(name);
            return (val != null && !val.trim().isEmpty()) ? Integer.parseInt(val.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static BigDecimal parseBigDecimalParam(HttpServletRequest req, String name, BigDecimal fallback) {
        try {
            String val = req.getParameter(name);
            return (val != null && !val.trim().isEmpty()) ? new BigDecimal(val.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean isSafeRedirect(String url) {
        if (url == null || url.isBlank()) return false;
        String trimmed = url.trim();
        return trimmed.startsWith("/") && !trimmed.startsWith("//");
    }
}
