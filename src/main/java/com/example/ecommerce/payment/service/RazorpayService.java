package com.example.ecommerce.payment.service;

import com.example.ecommerce.order.model.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Production-Grade Service Layer orchestrating Razorpay Payment Gateway integration (Live & Sandbox modes).
 * Supports full UPI Apps (Google Pay, PhonePe, Paytm, BHIM, CRED), Custom VPA, Dynamic QR, NetBanking, and Cards.
 * Implements cryptographic HMAC-SHA256 signature verification and server-side payment validation.
 */
@Service
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    private final String keyId;
    private final String keySecret;
    private final boolean isConfigured;

    public RazorpayService() {
        String resolvedKey = resolveEnvOrProperty("RAZORPAY_KEY_ID", "RAZORPAY_KEY", "RAZORPAY_LIVE_KEY_ID", "RAZORPAY_LIVE_KEY", "razorpay.key.id");
        String resolvedSecret = resolveEnvOrProperty("RAZORPAY_KEY_SECRET", "RAZORPAY_SECRET", "RAZORPAY_LIVE_KEY_SECRET", "RAZORPAY_LIVE_SECRET", "razorpay.key.secret");

        if (resolvedKey != null && !resolvedKey.trim().isEmpty() &&
            resolvedSecret != null && !resolvedSecret.trim().isEmpty()) {
            this.keyId = resolvedKey.trim();
            this.keySecret = resolvedSecret.trim();
            this.isConfigured = true;
            logger.info("Razorpay Payment Gateway initialized in LIVE/CONFIGURED mode with Key ID: {}", maskKey(this.keyId));
        } else {
            // Default Sandbox Test Mode Key for instant plug-and-play local development
            this.keyId = "";
            this.keySecret = "";
            this.isConfigured = false;
            logger.error("Razorpay Payment Gateway is disabled (credentials not found in environment). Running in Simulation mode.");
        }
    }

    public RazorpayService(String keyId, String keySecret) {
        if (keyId != null && !keyId.trim().isEmpty() && keySecret != null && !keySecret.trim().isEmpty()) {
            this.keyId = keyId.trim();
            this.keySecret = keySecret.trim();
            this.isConfigured = true;
        } else {
            this.keyId = "rzp_test_ShopKartSandbox";
            this.keySecret = "ShopKartTestSecretKey123";
            this.isConfigured = false;
        }
    }

    /**
     * Creates a Razorpay Order entity via Razorpay API (or generates simulated order if running in local sandbox).
     *
     * @param order the e-commerce Order entity
     * @return Razorpay Order ID (e.g. order_Nx8Y9z7...)
     */
    public String createRazorpayOrder(Order order) {
        if (order == null || order.getTotalAmount() == null) {
            throw new IllegalArgumentException("Order and order total amount must not be null.");
        }

        // Convert amount to paise (1 INR = 100 paise)
        long amountInPaise = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();

        if (isConfigured) {
            try {
                RazorpayClient razorpay = getRazorpayClient();

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amountInPaise);
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "rcpt_ord_" + order.getOrderId());
                orderRequest.put("payment_capture", 1); // Auto-capture payment

                JSONObject notes = new JSONObject();
                notes.put("ecommerce_order_id", String.valueOf(order.getOrderId()));
                notes.put("order_number", order.getOrderNumber());
                orderRequest.put("notes", notes);

                com.razorpay.Order rzpOrder = razorpay.orders.create(orderRequest);
                String rzpOrderId = rzpOrder.get("id");
                logger.info("Successfully created Razorpay Order [{}] for Order #{} (ID: {})", rzpOrderId, order.getOrderNumber(), order.getOrderId());
                return rzpOrderId;

            } catch (Exception e) {
                logger.error("Razorpay API order creation failed for Order ID #{}. Falling back to simulation.", order.getOrderId(), e);
            }
        }

        // Sandbox simulated fallback order ID
        String simulatedOrderId = "order_sim_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        logger.info("Generated Sandbox Simulated Order [{}] for Order ID #{}", simulatedOrderId, order.getOrderId());
        return simulatedOrderId;
    }

    /**
     * Cryptographically verifies HMAC-SHA256 signature returned by Razorpay Checkout.
     *
     * @param razorpayOrderId   Razorpay Order ID
     * @param razorpayPaymentId Razorpay Payment ID
     * @param razorpaySignature Cryptographic signature
     * @return true if authentic; false otherwise
     */
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if (razorpayOrderId == null || razorpayOrderId.trim().isEmpty() ||
            razorpayPaymentId == null || razorpayPaymentId.trim().isEmpty()) {
            return false;
        }

        if (razorpaySignature == null || razorpaySignature.trim().isEmpty()) {
            logger.warn("Rejecting verification: Missing Razorpay signature for Order: {}, Payment: {}", razorpayOrderId, razorpayPaymentId);
            return false;
        }

        if (isConfigured) {
            // Live or test key configured: enforce strict HMAC-SHA256 cryptographic verification
            try {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", razorpayOrderId.trim());
                options.put("razorpay_payment_id", razorpayPaymentId.trim());
                options.put("razorpay_signature", razorpaySignature.trim());

                boolean isValid = Utils.verifyPaymentSignature(options, keySecret);
                if (isValid) {
                    logger.info("Razorpay signature verified successfully for Order: {}, Payment: {}", razorpayOrderId, razorpayPaymentId);
                } else {
                    logger.error("Razorpay signature verification returned FALSE for Order: {}, Payment: {}", razorpayOrderId, razorpayPaymentId);
                }
                return isValid;
            } catch (Exception e) {
                logger.error("Razorpay signature verification failed with exception for Order: {}, Payment: {}", razorpayOrderId, razorpayPaymentId, e);
                return false;
            }
        }

        // Only allow fallback simulation in local sandbox when keys are not configured
        if (razorpayOrderId.startsWith("order_sim_") && razorpayPaymentId.startsWith("pay_sim_")) {
            logger.warn("Development Sandbox: Accepted simulated signature for Order [{}]", razorpayOrderId);
            return true;
        }

        logger.error("Razorpay signature verification failed: Gateway not configured and invalid simulation parameters.");
        return false;
    }

    /**
     * Performs strict server-side verification by querying Razorpay API directly.
     * Verifies payment status, amount matching, currency, and performs server-side capture if needed.
     *
     * @param razorpayPaymentId Razorpay Payment ID
     * @param expectedRazorpayOrderId Expected Razorpay Order ID (optional, checked if present)
     * @param expectedAmount Expected order amount
     * @return true if server-side validation and capture pass; false otherwise
     */
    public boolean verifyAndFetchPayment(String razorpayPaymentId, String expectedRazorpayOrderId, BigDecimal expectedAmount) {
        if (razorpayPaymentId == null || razorpayPaymentId.trim().isEmpty()) {
            return false;
        }

        if (!isConfigured) {
            if (razorpayPaymentId.startsWith("pay_sim_")) {
                logger.warn("Development Sandbox: Accepted simulated payment fetch for ID [{}]", razorpayPaymentId);
                return true;
            }
            logger.error("Server-side payment verification failed: Razorpay credentials not configured.");
            return false;
        }

        try {
            RazorpayClient razorpay = getRazorpayClient();
            com.razorpay.Payment payment = razorpay.payments.fetch(razorpayPaymentId.trim());

            if (payment == null) {
                logger.error("Server-side verification failed: Razorpay payment record not found for ID: {}", razorpayPaymentId);
                return false;
            }

            String status = payment.get("status");
            int amountInPaise = payment.get("amount");
            String currency = payment.get("currency");
            String rzpOrderId = payment.has("order_id") && payment.get("order_id") != null ? (String) payment.get("order_id") : null;

            long expectedPaise = expectedAmount.multiply(new BigDecimal(100)).longValue();

            // Validate currency and amount
            if (!"INR".equalsIgnoreCase(currency)) {
                logger.error("Currency mismatch for payment [{}]: expected INR, found {}", razorpayPaymentId, currency);
                return false;
            }

            if (amountInPaise != expectedPaise) {
                logger.error("Amount mismatch for payment [{}]: expected {} paise, found {} paise", razorpayPaymentId, expectedPaise, amountInPaise);
                return false;
            }

            if (expectedRazorpayOrderId != null && rzpOrderId != null && !expectedRazorpayOrderId.equalsIgnoreCase(rzpOrderId)) {
                logger.error("Order ID mismatch for payment [{}]: expected {}, found {}", razorpayPaymentId, expectedRazorpayOrderId, rzpOrderId);
                return false;
            }

            // Check if payment is captured
            if ("captured".equalsIgnoreCase(status)) {
                logger.info("Server-side payment [{}] verified as CAPTURED.", razorpayPaymentId);
                return true;
            } else if ("authorized".equalsIgnoreCase(status)) {
                // Manually capture authorized payment
                logger.info("Payment [{}] is AUTHORIZED. Initiating server-side capture for {} paise...", razorpayPaymentId, amountInPaise);
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount", amountInPaise);
                captureRequest.put("currency", "INR");
                com.razorpay.Payment capturedPayment = razorpay.payments.capture(razorpayPaymentId.trim(), captureRequest);
                String postCaptureStatus = capturedPayment.get("status");
                return "captured".equalsIgnoreCase(postCaptureStatus);
            } else {
                logger.warn("Payment [{}] in unaccepted state: {}", razorpayPaymentId, status);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error executing server-side payment verification for payment [{}]", razorpayPaymentId, e);
            return false;
        }
    }

    public RazorpayClient getRazorpayClient() throws Exception {
        return new RazorpayClient(keyId, keySecret);
    }

    public String getKeyId() {
        return keyId;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    private String resolveEnvOrProperty(String... keys) {
        for (String key : keys) {
            String val = System.getenv(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
            val = System.getProperty(key);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }
}
