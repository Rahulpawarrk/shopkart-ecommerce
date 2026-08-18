package com.example.ecommerce.cart.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Line Item inside a customer's Shopping Cart.
 * Always enriched with current live database product pricing, discounts, and inventory stock.
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int cartItemId;
    private int cartId;
    private int productId;
    private String productName;
    private String sku;
    private String brand;
    private String primaryImageUrl;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    private int stockQuantity = 0;
    private int quantity = 1;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartItem() {}

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl != null ? primaryImageUrl : "/assets/images/products/placeholder.png";
    }

    public String getImageUrl() {
        return getPrimaryImageUrl();
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage != null ? taxPercentage : BigDecimal.ZERO;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
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

    // Financial Calculations per Line Item
    public BigDecimal getDiscountAmount() {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return unitPrice.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscountedUnitPrice() {
        return unitPrice.subtract(getDiscountAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLineSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLineTotal() {
        return getDiscountedUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getLineTax() {
        if (taxPercentage == null || taxPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return getLineTotal().multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public boolean isAvailable() {
        return stockQuantity >= quantity && stockQuantity > 0;
    }

    public static CartItem fromProduct(com.example.ecommerce.product.model.Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(product.getProductId());
        item.setProductName(product.getProductName());
        item.setSku(product.getSku());
        item.setBrand(product.getBrand());
        item.setPrimaryImageUrl(product.getPrimaryImageUrl());
        item.setUnitPrice(product.getPrice());
        item.setDiscountPercentage(product.getDiscountPercentage());
        item.setTaxPercentage(product.getTaxPercentage());
        item.setStockQuantity(product.getStockQuantity());
        item.setQuantity(quantity > 0 ? quantity : 1);
        return item;
    }
}
