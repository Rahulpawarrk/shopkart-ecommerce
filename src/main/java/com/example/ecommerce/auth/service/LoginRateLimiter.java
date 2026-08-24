package com.example.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe IP + email based rate limiter for login attempts.
 * Blocks after 5 consecutive failures within a 15-minute window.
 */
public final class LoginRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimiter.class);

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutes
    private static final long LOCKOUT_MS = 15 * 60 * 1000L; // 15 minutes lockout

    private static final ConcurrentHashMap<String, AttemptEntry> store = new ConcurrentHashMap<>();

    private LoginRateLimiter() {}

    private static class AttemptEntry {
        int failureCount;
        long firstFailureTime;
        long lockedUntil;

        AttemptEntry(long now) {
            this.failureCount = 1;
            this.firstFailureTime = now;
            this.lockedUntil = 0;
        }
    }

    /**
     * Returns true if the given IP or email is currently blocked from login.
     */
    public static boolean isBlocked(String ip, String email) {
        long now = System.currentTimeMillis();
        String key = buildKey(ip, email);
        AttemptEntry entry = store.get(key);
        if (entry == null) return false;
        return entry.lockedUntil > now;
    }

    /**
     * Records a failed login attempt. Triggers lockout after MAX_FAILURES.
     */
    public static void recordFailure(String ip, String email) {
        long now = System.currentTimeMillis();
        String key = buildKey(ip, email);
        store.compute(key, (k, v) -> {
            if (v == null) return new AttemptEntry(now);
            if (now - v.firstFailureTime > WINDOW_MS) {
                v.failureCount = 1;
                v.firstFailureTime = now;
                v.lockedUntil = 0;
                return v;
            }
            v.failureCount++;
            if (v.failureCount >= MAX_FAILURES) {
                v.lockedUntil = now + LOCKOUT_MS;
                logger.warn("Login blocked for IP={} email={}: {} consecutive failures", ip, email, v.failureCount);
            }
            return v;
        });
    }

    /**
     * Clears failed attempts on successful login.
     */
    public static void clearFailures(String ip, String email) {
        store.remove(buildKey(ip, email));
    }

    /**
     * Returns remaining lockout minutes (0 if not locked).
     */
    public static long getRemainingLockoutMinutes(String ip, String email) {
        long now = System.currentTimeMillis();
        AttemptEntry entry = store.get(buildKey(ip, email));
        if (entry == null || entry.lockedUntil <= now) return 0;
        return Math.max(1, (entry.lockedUntil - now) / 60000 + 1);
    }

    private static String buildKey(String ip, String email) {
        return (ip != null ? ip : "unknown") + ":" + (email != null ? email.toLowerCase().trim() : "unknown");
    }
}
