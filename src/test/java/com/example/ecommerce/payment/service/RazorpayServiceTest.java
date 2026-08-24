package com.example.ecommerce.payment.service;

import com.example.ecommerce.order.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RazorpayService Unit Tests")
class RazorpayServiceTest {

    @Test
    @DisplayName("Should initialize in sandbox mode when no env credentials provided")
    void testDefaultSandboxInitialization() {
        RazorpayService razorpayService = new RazorpayService(null, null);
        assertFalse(razorpayService.isConfigured());
        assertEquals("rzp_test_ShopKartSandbox", razorpayService.getKeyId());
    }

    @Test
    @DisplayName("Should initialize in configured mode when valid keys provided")
    void testConfiguredInitialization() {
        RazorpayService razorpayService = new RazorpayService("rzp_live_testKey123", "secretVal456");
        assertTrue(razorpayService.isConfigured());
        assertEquals("rzp_live_testKey123", razorpayService.getKeyId());
    }

    @Test
    @DisplayName("Should create simulated Razorpay order ID in sandbox mode")
    void testCreateSimulatedRazorpayOrder() {
        RazorpayService razorpayService = new RazorpayService();

        Order order = new Order();
        order.setOrderId(101);
        order.setOrderNumber("ORD-2026-101");
        order.setTotalAmount(new BigDecimal("1499.00"));

        String orderId = razorpayService.createRazorpayOrder(order);

        assertNotNull(orderId);
        assertTrue(orderId.startsWith("order_"));
    }

    @Test
    @DisplayName("Should verify simulation payment signatures correctly")
    void testVerifySimulatedSignature() {
        RazorpayService razorpayService = new RazorpayService();

        boolean isValid = razorpayService.verifyPaymentSignature(
                "order_sim_1234567890", 
                "pay_sim_ABCDEF12345", 
                "sig_sim_sample"
        );

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject null or empty order and payment IDs")
    void testRejectInvalidSignatureParameters() {
        RazorpayService razorpayService = new RazorpayService();

        assertFalse(razorpayService.verifyPaymentSignature(null, "pay_123", "sig_123"));
        assertFalse(razorpayService.verifyPaymentSignature("order_123", null, "sig_123"));
    }
}
