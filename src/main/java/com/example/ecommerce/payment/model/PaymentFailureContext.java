package com.example.ecommerce.payment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Encapsulates secure one-time payment failure flash context stored in user session.
 * Prevents exposing internal order IDs, database primary keys, or raw exception traces in the browser address bar.
 */
public class PaymentFailureContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int orderId;
    private final String orderNumber;
    private final int userId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String failureReason;
    private final String transactionReference;
    private final LocalDateTime failedAt;
    private final boolean canRetry;
    private final boolean canSwitchToCod;

    public PaymentFailureContext(int orderId, String orderNumber, int userId, BigDecimal amount,
                                 String paymentMethod, String failureReason, String transactionReference,
                                 boolean canRetry, boolean canSwitchToCod) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.failureReason = failureReason != null ? failureReason : "Your transaction could not be completed.";
        this.transactionReference = transactionReference;
        this.failedAt = LocalDateTime.now();
        this.canRetry = canRetry;
        this.canSwitchToCod = canSwitchToCod;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public boolean isCanRetry() {
        return canRetry;
    }

    public boolean isCanSwitchToCod() {
        return canSwitchToCod;
    }
}
