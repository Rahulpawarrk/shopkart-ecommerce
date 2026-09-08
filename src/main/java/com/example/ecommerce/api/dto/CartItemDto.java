package com.example.ecommerce.api.dto;

import com.example.ecommerce.cart.model.CartItem;
import java.math.BigDecimal;

public class CartItemDto {
    private int cartItemId;
    private int productId;
    private String productName;
    private String productSlug;
    private String brand;
    private String primaryImageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal effectivePrice;
    private BigDecimal lineTotal;
    private int availableStock;
    private boolean inStock;

    public CartItemDto() {}

    public static CartItemDto fromEntity(CartItem item) {
        if (item == null) return null;
        CartItemDto dto = new CartItemDto();
        dto.setCartItemId(item.getCartItemId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setEffectivePrice(item.getDiscountedUnitPrice());
        dto.setLineTotal(item.getLineTotal());
        dto.setProductName(item.getProductName());
        dto.setProductSlug(String.valueOf(item.getProductId()));
        dto.setBrand(item.getBrand());
        dto.setPrimaryImageUrl(item.getPrimaryImageUrl());
        dto.setAvailableStock(item.getStockQuantity());
        dto.setInStock(item.isAvailable());
        return dto;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
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

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
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

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
