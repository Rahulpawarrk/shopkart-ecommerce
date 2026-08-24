package com.example.ecommerce.payment.service;

import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.payment.dao.PaymentDAO;
import com.example.ecommerce.payment.model.Payment;
import com.example.ecommerce.payment.model.PaymentTransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests with Mockito")
class PaymentServiceTest {

    @Mock
    private PaymentDAO paymentDAO;

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private com.example.ecommerce.payment.dao.PaymentReconciliationDAO paymentReconciliationDAO;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should successfully initiate payment intent and generate transaction reference")
    void testInitiatePayment() {
        Order order = new Order();
        order.setOrderId(10);
        order.setTotalAmount(new BigDecimal("4999.00"));

        when(paymentDAO.createPayment(any(Payment.class))).thenReturn(101);

        Payment payment = paymentService.initiatePayment(order, "UPI");

        assertNotNull(payment);
        assertEquals(101, payment.getPaymentId());
        assertTrue(payment.getTransactionReference().startsWith("PAY-UPI-"));
        assertEquals(PaymentTransactionStatus.PENDING, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("Should reject payment callback if user ID does not match order ownership")
    void testUnauthorizedPaymentCallback() {
        Order order = new Order();
        order.setOrderId(5);
        order.setUserId(1); // Belongs to user 1

        when(orderDAO.findById(5)).thenReturn(Optional.of(order));

        // User 2 attempts to process payment callback
        assertThrows(ValidationException.class, () ->
            paymentService.processGatewayCallback(5, 2, "TXN-REF", null, true, "OK")
        );
    }

    @Test
    @DisplayName("Should return true immediately if order is already PAID (Idempotency)")
    void testIdempotentPaymentCallback() throws Exception {
        Order order = new Order();
        order.setOrderId(8);
        order.setUserId(1);
        order.setPaymentStatus(com.example.ecommerce.order.model.PaymentStatus.PAID);

        when(orderDAO.findById(8)).thenReturn(Optional.of(order));

        boolean result = paymentService.processGatewayCallback(8, 1, "TXN-REF", null, true, "OK");
        assertTrue(result);
        verify(paymentDAO, never()).updatePaymentStatusByOrderId(anyInt(), any(), any(), any());
    }

    @Test
    @DisplayName("Should successfully update reconciliation resolution notes and status")
    void testUpdateReconciliationResolution() throws Exception {
        when(paymentReconciliationDAO.updateResolution(1, "VERIFIED_DEBITED", "Amount deducted from customer bank UTR: 12345", 99))
                .thenReturn(true);

        boolean updated = paymentService.updateReconciliationResolution(1, "VERIFIED_DEBITED", "Amount deducted from customer bank UTR: 12345", 99);
        assertTrue(updated);
        verify(paymentReconciliationDAO).updateResolution(1, "VERIFIED_DEBITED", "Amount deducted from customer bank UTR: 12345", 99);
    }

    @Test
    @DisplayName("Should retrieve reconciliation statistics summary")
    void testGetReconciliationStats() {
        java.util.Map<String, Object> mockStats = new java.util.HashMap<>();
        mockStats.put("totalFailed", 5);
        mockStats.put("pendingAudit", 2);
        mockStats.put("totalDisputedAmount", new BigDecimal("12500.00"));

        when(paymentReconciliationDAO.getReconciliationStats()).thenReturn(mockStats);

        java.util.Map<String, Object> stats = paymentService.getReconciliationStats();
        assertNotNull(stats);
        assertEquals(5, stats.get("totalFailed"));
        assertEquals(2, stats.get("pendingAudit"));
    }
}
