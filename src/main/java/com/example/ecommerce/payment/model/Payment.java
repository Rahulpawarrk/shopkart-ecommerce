package com.example.ecommerce.payment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity recording all payment gateway attempts, transactions, and refunds.
 */
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int paymentId;
    private int orderId;
    private String orderNumber;
    private String customerName;
    private String paymentMethod;
    private String transactionReference;
    private BigDecimal amount = BigDecimal.ZERO;
    private PaymentTransactionStatus paymentStatus = PaymentTransactionStatus.PENDING;
    private String gatewayResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {}

    public Payment(int orderId, String paymentMethod, String transactionReference, 
                   BigDecimal amount, PaymentTransactionStatus paymentStatus, String gatewayResponse) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.gatewayResponse = gatewayResponse;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentTransactionStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentTransactionStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
