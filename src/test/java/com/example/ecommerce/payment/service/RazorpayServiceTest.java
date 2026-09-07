package com.example.ecommerce.payment.service;

import com.example.ecommerce.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RazorpayService Unit Tests")
class RazorpayServiceTest {

    @Test
    @DisplayName("Should initialize in unconfigured mode when no env credentials provided")
    void testDefaultUnconfiguredInitialization() {
        RazorpayService razorpayService = new RazorpayService(null, null);
        assertFalse(razorpayService.isConfigured());
        assertEquals("", razorpayService.getKeyId());
    }

    @Test
    @DisplayName("Should initialize in configured mode when valid keys provided")
    void testConfiguredInitialization() {
        RazorpayService razorpayService = new RazorpayService("rzp_live_testKey123", "secretVal456");
        assertTrue(razorpayService.isConfigured());
        assertEquals("rzp_live_testKey123", razorpayService.getKeyId());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when creating Razorpay order without configuration")
    void testCreateRazorpayOrderUnconfigured() {
        RazorpayService razorpayService = new RazorpayService(null, null);

        Order order = new Order();
        order.setOrderId(101);
        order.setOrderNumber("ORD-2026-101");
        order.setTotalAmount(new BigDecimal("1499.00"));

        assertThrows(IllegalStateException.class, () -> razorpayService.createRazorpayOrder(order));
    }

    @Test
    @DisplayName("Should reject payment signatures when gateway is unconfigured")
    void testRejectUnconfiguredSignature() {
        RazorpayService razorpayService = new RazorpayService(null, null);

        boolean isValid = razorpayService.verifyPaymentSignature(
                "order_1234567890", 
                "pay_ABCDEF12345", 
                "sig_sample"
        );

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should cryptographically verify valid HMAC-SHA256 signatures in configured mode")
    void testVerifyCryptographicSignatureInConfiguredMode() throws Exception {
        String testSecret = "TestSecretKey12345";
        RazorpayService razorpayService = new RazorpayService("rzp_test_key", testSecret);

        String orderId = "order_live_987654";
        String paymentId = "pay_live_123456";
        String payload = orderId + "|" + paymentId;

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(testSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        String validSignature = java.util.HexFormat.of().formatHex(mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        // Valid signature must pass
        assertTrue(razorpayService.verifyPaymentSignature(orderId, paymentId, validSignature));

        // Tampered signature must be rejected
        assertFalse(razorpayService.verifyPaymentSignature(orderId, paymentId, "invalid_tampered_signature"));

        // Simulated IDs must NOT bypass configured mode with invalid signature
        assertFalse(razorpayService.verifyPaymentSignature("order_sim_123", "pay_sim_456", "sig_sim_sample"));
    }

    @Test
    @DisplayName("Should reject null or empty order and payment IDs")
    void testRejectInvalidSignatureParameters() {
        RazorpayService razorpayService = new RazorpayService(null, null);

        assertFalse(razorpayService.verifyPaymentSignature(null, "pay_123", "sig_123"));
        assertFalse(razorpayService.verifyPaymentSignature("order_123", null, "sig_123"));
        assertFalse(razorpayService.verifyPaymentSignature("", "pay_123", "sig_123"));
    }
}
