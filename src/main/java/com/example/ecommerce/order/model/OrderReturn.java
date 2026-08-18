package com.example.ecommerce.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Order Return & Replacement Entity.
 */
public class OrderReturn implements Serializable {
    private static final long serialVersionUID = 1L;

    private int returnId;
    private int orderId;
    private String orderNumber;
    private int userId;
    private String customerName;
    private String customerEmail;
    private String returnNumber;
    private String returnReason;
    private String resolutionType = "REFUND"; // REFUND, REPLACEMENT, EXCHANGE
    private String comments;
    private String imageUrl;
    private String returnStatus = "REQUESTED"; // REQUESTED, APPROVED, PICKUP_SCHEDULED, ITEM_RECEIVED, REFUNDED, REPLACED, REJECTED
    private BigDecimal refundAmount;
    private String adminNotes;
    private LocalDateTime pickupDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderReturn() {}

    public OrderReturn(int orderId, int userId, String returnNumber, String returnReason, String resolutionType, String comments, String imageUrl) {
        this.orderId = orderId;
        this.userId = userId;
        this.returnNumber = returnNumber;
        this.returnReason = returnReason;
        this.resolutionType = resolutionType;
        this.comments = comments;
        this.imageUrl = imageUrl;
        this.returnStatus = "REQUESTED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getReturnId() {
        return returnId;
    }

    public void setReturnId(int returnId) {
        this.returnId = returnId;
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

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(String resolutionType) {
        this.resolutionType = resolutionType;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public LocalDateTime getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDateTime pickupDate) {
        this.pickupDate = pickupDate;
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
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getFormattedPickupDate() {
        if (pickupDate == null) return "";
        return pickupDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getStatusBadgeClass() {
        if ("REFUNDED".equalsIgnoreCase(returnStatus) || "REPLACED".equalsIgnoreCase(returnStatus)) {
            return "badge-success";
        } else if ("APPROVED".equalsIgnoreCase(returnStatus) || "PICKUP_SCHEDULED".equalsIgnoreCase(returnStatus)) {
            return "badge-info";
        } else if ("REJECTED".equalsIgnoreCase(returnStatus)) {
            return "badge-danger";
        } else {
            return "badge-warning";
        }
    }
}
