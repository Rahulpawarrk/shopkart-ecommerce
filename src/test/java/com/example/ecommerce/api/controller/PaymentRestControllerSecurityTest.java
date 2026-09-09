package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.VerifyPaymentRequest;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.payment.service.RazorpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentRestControllerSecurityTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private PaymentRestController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentRestController(orderService, paymentService, razorpayService);
        System.clearProperty("allow.payment.simulation");
    }

    @Test
    @DisplayName("Calling /api/payments/verify when Razorpay is unconfigured returns 503 SERVICE_UNAVAILABLE and rejects simulation")
    void testVerifyWithoutConfiguredGatewayReturns503() {
        UserSession user = new UserSession(10, "customer@example.com", "John", "Doe", Set.of("CUSTOMER"));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(user);

        Order order = new Order();
        order.setOrderId(101);
        order.setOrderNumber("ORD-101");
        order.setUserId(10);
        when(orderService.getOrderById(101, 10)).thenReturn(order);

        when(razorpayService.isConfigured()).thenReturn(false);

        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setOrderId(101);
        req.setTransactionReference("DEV-SIM-12345");

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.verifyPayment(req, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        verify(paymentService, never()).processGatewayCallback(anyInt(), anyInt(), anyString(), any(), anyBoolean(), anyString());
    }

    @Test
    @DisplayName("Webhook with missing signature returns 400 Bad Request")
    void testWebhookMissingSignatureReturns400() {
        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.handleRazorpayWebhook("{}", null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Webhook with invalid cryptographic signature returns 401 Unauthorized")
    void testWebhookInvalidSignatureReturns401() {
        when(razorpayService.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.handleRazorpayWebhook("{\"event\":\"payment.captured\"}", "invalid_sig");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
