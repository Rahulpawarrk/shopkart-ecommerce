package com.example.ecommerce.wishlist.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Item inside customer Wishlist enriched with live catalog metadata and stock.
 */
@Entity
@Table(name = "wishlist_items", schema = "dbo")
public class WishlistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_item_id")
    private int wishlistItemId;

    @Column(name = "wishlist_id", nullable = false)
    private int wishlistId;

    @Column(name = "product_id", nullable = false)
    private int productId;

    @Transient
    private String productName;

    @Transient
    private String sku;

    @Transient
    private String brand;

    @Transient
    private BigDecimal price = BigDecimal.ZERO;

    @Transient
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Transient
    private String primaryImageUrl;

    @Transient
    private int stockQuantity = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
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
