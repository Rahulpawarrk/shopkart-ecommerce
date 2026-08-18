package com.example.ecommerce.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order Entity representing a confirmed customer order, immutable address snapshots,
 * and point-in-time financial amounts.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderId;
    private String orderNumber;
    private int userId;
    private String customerName;
    private String customerEmail;
    private OrderStatus orderStatus = OrderStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private String paymentMethod = "COD";

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Integer couponId;

    // Shipping Address Snapshot
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    private String billingAddressSnapshot;
    private String notes;

    // Logistics & Fulfillment Tracking
    private String courierPartner;
    private String trackingNumber;
    private String deliveryAgentPhone;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItem> items = new ArrayList<>();
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public Order() {}

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

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public String getShippingFullName() {
        return shippingFullName;
    }

    public void setShippingFullName(String shippingFullName) {
        this.shippingFullName = shippingFullName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    public void setShippingAddressLine1(String shippingAddressLine1) {
        this.shippingAddressLine1 = shippingAddressLine1;
    }

    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    public void setShippingAddressLine2(String shippingAddressLine2) {
        this.shippingAddressLine2 = shippingAddressLine2;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String shippingPostalCode) {
        this.shippingPostalCode = shippingPostalCode;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getBillingAddressSnapshot() {
        return billingAddressSnapshot;
    }

    public void setBillingAddressSnapshot(String billingAddressSnapshot) {
        this.billingAddressSnapshot = billingAddressSnapshot;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<OrderItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public int getTotalItems() {
        if (items == null || items.isEmpty()) return 1;
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total > 0 ? total : 1;
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return statusHistory != null ? statusHistory : Collections.emptyList();
    }

    public void setStatusHistory(List<OrderStatusHistory> statusHistory) {
        this.statusHistory = statusHistory != null ? statusHistory : new ArrayList<>();
    }

    public String getFormattedShippingAddress() {
        StringBuilder sb = new StringBuilder();
        if (shippingFullName != null) sb.append(shippingFullName).append(", ");
        if (shippingAddressLine1 != null) sb.append(shippingAddressLine1);
        if (shippingAddressLine2 != null && !shippingAddressLine2.trim().isEmpty()) sb.append(", ").append(shippingAddressLine2);
        if (shippingCity != null) sb.append(", ").append(shippingCity);
        if (shippingState != null) sb.append(", ").append(shippingState);
        if (shippingPostalCode != null) sb.append(" - ").append(shippingPostalCode);
        if (shippingCountry != null) sb.append(", ").append(shippingCountry);
        if (shippingPhone != null) sb.append(" (Phone: ").append(shippingPhone).append(")");
        return sb.toString();
    }

    public String getCourierPartner() {
        return courierPartner;
    }

    public void setCourierPartner(String courierPartner) {
        this.courierPartner = courierPartner;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getDeliveryAgentPhone() {
        return deliveryAgentPhone;
    }

    public void setDeliveryAgentPhone(String deliveryAgentPhone) {
        this.deliveryAgentPhone = deliveryAgentPhone;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public int getMilestoneStep() {
        return orderStatus != null ? orderStatus.getMilestoneStep() : 1;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return createdAt.format(formatter);
    }

    public String getFormattedDeliveredAt() {
        if (deliveredAt == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return deliveredAt.format(formatter);
    }

    public String getFormattedDeliveredDate() {
        LocalDateTime dt = deliveredAt != null ? deliveredAt : (orderStatus == OrderStatus.DELIVERED ? (updatedAt != null ? updatedAt : createdAt) : null);
        if (dt == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dt.format(formatter);
    }

    public String getFormattedEstimatedDeliveryDate() {
        if (estimatedDeliveryDate == null) return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
        return estimatedDeliveryDate.format(formatter);
    }

    public boolean isDispatchedOrLater() {
        return orderStatus == OrderStatus.DISPATCHED 
            || orderStatus == OrderStatus.SHIPPED 
            || orderStatus == OrderStatus.IN_TRANSIT 
            || orderStatus == OrderStatus.OUT_FOR_DELIVERY 
            || orderStatus == OrderStatus.DELIVERED;
    }

    public long getCreatedAtEpochMillis() {
        if (createdAt == null) return 0L;
        return createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public boolean isCancellable() {
        return orderStatus == OrderStatus.PENDING 
            || orderStatus == OrderStatus.CONFIRMED 
            || orderStatus == OrderStatus.PROCESSING;
    }
}
