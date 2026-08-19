package com.example.ecommerce.auth.service;

import com.example.ecommerce.config.RedisManager;
import com.example.ecommerce.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe rate limiter and lockout manager for OTP generation requests.
 * Enforces a strict maximum of 3 OTP requests within a 1-hour rolling window.
 * If exceeded, locks out the identifier for 1 hour (3600 seconds).
 */
public class OtpRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(OtpRateLimiter.class);

    private static final int MAX_OTP_REQUESTS = 3;
    private static final long LOCKOUT_DURATION_MS = 60 * 60 * 1000L; // 1 Hour in ms
    private static final int LOCKOUT_DURATION_SEC = 3600; // 1 Hour in seconds

    private static final int MAX_VERIFY_FAILURES = 5;
    private static final long VERIFY_LOCKOUT_MS = 15 * 60 * 1000L; // 15 Minutes
    private static final int VERIFY_LOCKOUT_SEC = 900;

    private static final ConcurrentHashMap<String, RateLimitEntry> inMemoryStore = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FailedAttemptEntry> failedAttemptsStore = new ConcurrentHashMap<>();

    private static class RateLimitEntry {
        int requestCount;
        long windowStartTime;
        long lockedUntil;

        RateLimitEntry(long now) {
            this.requestCount = 1;
            this.windowStartTime = now;
            this.lockedUntil = 0;
        }
    }

    private static class FailedAttemptEntry {
        int failureCount;
        long firstFailureTime;
        long lockedUntil;

        FailedAttemptEntry(long now) {
            this.failureCount = 1;
            this.firstFailureTime = now;
            this.lockedUntil = 0;
        }
    }

    private OtpRateLimiter() {}

    /**
     * Checks rate limits and increments the request count for a given identifier (email/phone).
     *
     * @param identifier  email address or clean phone number
     * @param actionType  "REGISTRATION" or "PASSWORD_RESET"
     * @throws ValidationException if the identifier is currently locked out or exceeded 3 requests
     */
    public static void checkAndIncrement(String identifier, String actionType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return;
        }

        String target = identifier.trim().toLowerCase();
        String lockKey = "shopkart:otp_lock:" + actionType.toLowerCase() + ":" + target;
        String countKey = "shopkart:otp_count:" + actionType.toLowerCase() + ":" + target;
        long now = System.currentTimeMillis();

        // 1. Check Redis Lockout if Redis is active
        if (RedisManager.isAvailable()) {
            String locked = RedisManager.get(lockKey);
            if (locked != null) {
                try {
                    long lockExpiryMs = Long.parseLong(locked);
                    long remainingMinutes = Math.max(1, (lockExpiryMs - now) / 60000 + 1);
                    logger.warn("OTP request blocked for {} [action={}]: Locked for {} more minutes (Redis)", target, actionType, remainingMinutes);
                    throw new ValidationException(
                            "You have exceeded the maximum of " + MAX_OTP_REQUESTS + " OTP requests. To protect your account, OTP generation is locked for 1 hour. Please try again in " + remainingMinutes + " minute(s)."
                    );
                } catch (NumberFormatException ignored) {}
            }
        }

        // 2. Check In-Memory Lockout
        RateLimitEntry entry = inMemoryStore.compute(target, (k, v) -> {
            if (v == null) {
                return new RateLimitEntry(now);
            }

            // Check if currently locked out
            if (v.lockedUntil > now) {
                return v;
            }

            // Reset window if 1 hour has elapsed since window started
            if (now - v.windowStartTime > LOCKOUT_DURATION_MS) {
                v.requestCount = 1;
                v.windowStartTime = now;
                v.lockedUntil = 0;
                return v;
            }

            // Increment request count
            v.requestCount++;
            if (v.requestCount > MAX_OTP_REQUESTS) {
                v.lockedUntil = now + LOCKOUT_DURATION_MS;
            }
            return v;
        });

        // 3. Handle Active Lockout
        if (entry.lockedUntil > now) {
            long remainingMinutes = Math.max(1, (entry.lockedUntil - now) / 60000 + 1);

            // Persist Lockout to Redis as well
            if (RedisManager.isAvailable()) {
                RedisManager.setex(lockKey, LOCKOUT_DURATION_SEC, String.valueOf(entry.lockedUntil));
            }

            logger.warn("OTP request blocked for {} [action={}]: Exceeded {} attempts. Locked for {} more minute(s)",
                    target, actionType, MAX_OTP_REQUESTS, remainingMinutes);

            throw new ValidationException(
                    "You have exceeded the maximum of " + MAX_OTP_REQUESTS + " OTP requests. To protect your account, OTP generation is temporarily locked for 1 hour. Please try again in " + remainingMinutes + " minute(s)."
            );
        }

        // Sync count to Redis for distributed environments
        if (RedisManager.isAvailable()) {
            RedisManager.setex(countKey, LOCKOUT_DURATION_SEC, String.valueOf(entry.requestCount));
        }

        logger.info("OTP request accepted for {} [action={}] (Attempt {} of {})", target, actionType, entry.requestCount, MAX_OTP_REQUESTS);
    }

    /**
     * Checks if the identifier is locked out due to too many failed OTP verification attempts.
     */
    public static void checkFailedVerification(String identifier, String actionType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return;
        }

        String target = identifier.trim().toLowerCase();
        String verifyLockKey = "shopkart:otp_verify_lock:" + actionType.toLowerCase() + ":" + target;
        long now = System.currentTimeMillis();

        if (RedisManager.isAvailable()) {
            String locked = RedisManager.get(verifyLockKey);
            if (locked != null) {
                try {
                    long lockExpiryMs = Long.parseLong(locked);
                    if (lockExpiryMs > now) {
                        long remainingMinutes = Math.max(1, (lockExpiryMs - now) / 60000 + 1);
                        throw new ValidationException("Too many failed verification attempts. Please try again in " + remainingMinutes + " minute(s).");
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        FailedAttemptEntry entry = failedAttemptsStore.get(target);
        if (entry != null && entry.lockedUntil > now) {
            long remainingMinutes = Math.max(1, (entry.lockedUntil - now) / 60000 + 1);
            throw new ValidationException("Too many failed verification attempts. Please try again in " + remainingMinutes + " minute(s).");
        }
    }

    /**
     * Records a failed verification attempt. If maximum allowed failures exceeded, locks out the identifier.
     */
    public static void recordFailedVerification(String identifier, String actionType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return;
        }

        String target = identifier.trim().toLowerCase();
        String verifyLockKey = "shopkart:otp_verify_lock:" + actionType.toLowerCase() + ":" + target;
        long now = System.currentTimeMillis();

        FailedAttemptEntry entry = failedAttemptsStore.compute(target, (k, v) -> {
            if (v == null) {
                return new FailedAttemptEntry(now);
            }
            if (now - v.firstFailureTime > VERIFY_LOCKOUT_MS) {
                v.failureCount = 1;
                v.firstFailureTime = now;
                v.lockedUntil = 0;
                return v;
            }
            v.failureCount++;
            if (v.failureCount >= MAX_VERIFY_FAILURES) {
                v.lockedUntil = now + VERIFY_LOCKOUT_MS;
            }
            return v;
        });

        if (entry.lockedUntil > now) {
            if (RedisManager.isAvailable()) {
                RedisManager.setex(verifyLockKey, VERIFY_LOCKOUT_SEC, String.valueOf(entry.lockedUntil));
            }
            long remainingMinutes = Math.max(1, (entry.lockedUntil - now) / 60000 + 1);
            logger.warn("OTP verification locked out for {} [action={}]: Exceeded {} failed attempts.", target, actionType, MAX_VERIFY_FAILURES);
            throw new ValidationException("Too many incorrect OTP attempts. For security, account verification is locked for " + remainingMinutes + " minute(s).");
        }
    }

    /**
     * Resets both the rate limiter and failed attempts for an identifier upon successful verification.
     */
    public static void reset(String identifier, String actionType) {
        if (identifier == null) return;
        String target = identifier.trim().toLowerCase();
        inMemoryStore.remove(target);
        failedAttemptsStore.remove(target);
        if (RedisManager.isAvailable()) {
            RedisManager.del("shopkart:otp_lock:" + actionType.toLowerCase() + ":" + target);
            RedisManager.del("shopkart:otp_count:" + actionType.toLowerCase() + ":" + target);
            RedisManager.del("shopkart:otp_verify_lock:" + actionType.toLowerCase() + ":" + target);
        }
    }
}
