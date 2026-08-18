package com.example.ecommerce.payment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity representing a failed / disputed payment transaction
 * logged for admin audit and reconciliation (handling bank debits, customer queries, and refunds).
 */
public class PaymentReconciliation implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private int reconciliationId;
    private int orderId;
    private String orderNumber;
    private int userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String transactionReference;
    private String gatewayOrderId;
    private String paymentMethod;
    private BigDecimal amount = BigDecimal.ZERO;
    private String failureReason;
    private String gatewayResponse;
    private String reconciliationStatus = "PENDING";
    private String adminNotes;
    private Integer resolvedBy;
    private String resolverName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentReconciliation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentReconciliation(int orderId, int userId, String transactionReference, 
                                 String gatewayOrderId, String paymentMethod, BigDecimal amount, 
                                 String failureReason, String gatewayResponse) {
        this.orderId = orderId;
        this.userId = userId;
        this.transactionReference = transactionReference;
        this.gatewayOrderId = gatewayOrderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.failureReason = failureReason;
        this.gatewayResponse = gatewayResponse;
        this.reconciliationStatus = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getReconciliationId() {
        return reconciliationId;
    }

    public void setReconciliationId(int reconciliationId) {
        this.reconciliationId = reconciliationId;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public String getReconciliationStatus() {
        return reconciliationStatus;
    }

    public void setReconciliationStatus(String reconciliationStatus) {
        this.reconciliationStatus = reconciliationStatus;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Integer getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Integer resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolverName() {
        return resolverName;
    }

    public void setResolverName(String resolverName) {
        this.resolverName = resolverName;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DISPLAY_FORMAT) : "";
    }

    public String getFormattedResolvedAt() {
        return resolvedAt != null ? resolvedAt.format(DISPLAY_FORMAT) : "";
    }

    public String getStatusBadgeClass() {
        if (reconciliationStatus == null) return "badge-secondary";
        return switch (reconciliationStatus.toUpperCase()) {
            case "RESOLVED", "REFUND_COMPLETED" -> "badge-success";
            case "VERIFIED_DEBITED", "REFUND_INITIATED" -> "badge-warning";
            case "MANUALLY_CREDITED" -> "badge-primary";
            case "NOT_DEBITED" -> "badge-secondary";
            default -> "badge-danger";
        };
    }
}
