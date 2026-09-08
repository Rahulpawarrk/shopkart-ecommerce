package com.example.ecommerce.api.dto;

import com.example.ecommerce.order.model.OrderItem;
import java.math.BigDecimal;

public class OrderItemDto {
    private int orderItemId;
    private int orderId;
    private int productId;
    private String productName;
    private String sku;
    private String primaryImageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal effectivePrice;
    private BigDecimal lineTotal;

    public OrderItemDto() {}

    public static OrderItemDto fromEntity(OrderItem item) {
        if (item == null) return null;
        OrderItemDto dto = new OrderItemDto();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setOrderId(item.getOrderId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrimaryImageUrl(item.getPrimaryImageUrl() != null && !item.getPrimaryImageUrl().trim().isEmpty()
                ? item.getPrimaryImageUrl()
                : null);
        dto.setUnitPrice(item.getUnitPrice());
        BigDecimal discountAmt = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        dto.setEffectivePrice(unitPrice.subtract(discountAmt));
        dto.setDiscountPercentage(unitPrice.compareTo(BigDecimal.ZERO) > 0 && discountAmt.compareTo(BigDecimal.ZERO) > 0
                ? discountAmt.divide(unitPrice, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        dto.setLineTotal(item.getLineTotal());
        return dto;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getEffectivePrice() {
        return effectivePrice;
    }

    public void setEffectivePrice(BigDecimal effectivePrice) {
        this.effectivePrice = effectivePrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
