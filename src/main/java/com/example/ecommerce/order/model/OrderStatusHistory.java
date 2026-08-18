package com.example.ecommerce.order.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Historical status transition audit record for an Order.
 */
public class OrderStatusHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int historyId;
    private int orderId;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private Integer changedBy;
    private String changedByName;
    private String remarks;
    private LocalDateTime createdAt;

    public OrderStatusHistory() {}

    public OrderStatusHistory(int orderId, OrderStatus previousStatus, OrderStatus newStatus, 
                              Integer changedBy, String remarks) {
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.createdAt = LocalDateTime.now();
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Integer getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Integer changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return createdAt.format(formatter);
    }
}
