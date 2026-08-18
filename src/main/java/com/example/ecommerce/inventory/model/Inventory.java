package com.example.ecommerce.inventory.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Model representing real-time product stock levels and alert thresholds.
 */
public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int inventoryId;
    private int productId;
    private String productName;
    private String sku;
    private String brand;
    private String categoryName;
    private BigDecimal price;
    private int quantity;
    private int lowStockThreshold;
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public Inventory(int inventoryId, int productId, int quantity, int lowStockThreshold, LocalDateTime lastUpdated) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.lastUpdated = lastUpdated;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

    public boolean isLowStock() {
        return quantity > 0 && quantity <= lowStockThreshold;
    }

    public String getStockStatus() {
        if (isOutOfStock()) return "OUT_OF_STOCK";
        if (isLowStock()) return "LOW_STOCK";
        return "IN_STOCK";
    }
}
