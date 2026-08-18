package com.example.ecommerce.wishlist.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Item inside customer Wishlist enriched with live catalog metadata and stock.
 */
public class WishlistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wishlistItemId;
    private int wishlistId;
    private int productId;
    private String productName;
    private String sku;
    private String brand;
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    private String primaryImageUrl;
    private int stockQuantity = 0;
    private LocalDateTime createdAt;

    public WishlistItem() {}

    public int getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(int wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl != null ? primaryImageUrl : "/assets/images/products/placeholder.png";
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getDiscountedPrice() {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discountAmount = price.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getEffectivePrice() {
        return getDiscountedPrice();
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }
}
