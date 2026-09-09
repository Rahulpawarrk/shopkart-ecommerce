package com.example.ecommerce.payment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RazorpayWebhookVerificationTest {

    @Test
    @DisplayName("verifyWebhookSignature returns false when payload or signature is null or empty")
    void testBlankPayloadOrSignature() {
        RazorpayService service = new RazorpayService("rzp_test_123", "secret123", "webhookSecret123");
        assertFalse(service.verifyWebhookSignature(null, "sig"));
        assertFalse(service.verifyWebhookSignature("", "sig"));
        assertFalse(service.verifyWebhookSignature("{\"event\":\"test\"}", null));
        assertFalse(service.verifyWebhookSignature("{\"event\":\"test\"}", ""));
    }

    @Test
    @DisplayName("verifyWebhookSignature returns false when webhook secret is not configured")
    void testUnconfiguredWebhookSecret() {
        RazorpayService service = new RazorpayService("rzp_test_123", "secret123", "");
        assertFalse(service.verifyWebhookSignature("{\"event\":\"test\"}", "any_signature"));
    }
}
