package com.example.ecommerce.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Encapsulates secure one-time order confirmation flash context stored in user session.
 * Prevents exposing raw order IDs, order numbers, or transaction references in the browser address bar.
 */
public class OrderConfirmationContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int orderId;
    private final String orderNumber;
    private final int userId;
    private final BigDecimal totalAmount;
    private final String paymentMethod;
    private final String paymentStatus;
    private final String transactionReference;
    private final LocalDateTime confirmedAt;
    private final boolean justPlaced;

    public OrderConfirmationContext(int orderId, String orderNumber, int userId, BigDecimal totalAmount,
                                    String paymentMethod, String paymentStatus, String transactionReference) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.confirmedAt = LocalDateTime.now();
        this.justPlaced = true;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getUserId() {
        return userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public boolean isJustPlaced() {
        return justPlaced;
    }
}
