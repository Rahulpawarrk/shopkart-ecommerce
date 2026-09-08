package com.example.ecommerce.api.dto;

import com.example.ecommerce.wishlist.model.WishlistItem;
import java.math.BigDecimal;

public class WishlistItemDto {
    private int wishlistItemId;
    private int wishlistId;
    private int productId;
    private String productName;
    private String sku;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private BigDecimal effectivePrice;
    private String primaryImageUrl;
    private int stockQuantity;
    private boolean inStock;

    public WishlistItemDto() {}

    public static WishlistItemDto fromEntity(WishlistItem item) {
        if (item == null) return null;
        WishlistItemDto dto = new WishlistItemDto();
        dto.setWishlistItemId(item.getWishlistItemId());
        dto.setWishlistId(item.getWishlistId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setSku(item.getSku());
        dto.setBrand(item.getBrand());
        dto.setPrice(item.getPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setEffectivePrice(item.getEffectivePrice());
        dto.setPrimaryImageUrl(item.getPrimaryImageUrl());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setInStock(item.isInStock());
        return dto;
    }

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
        this.price = price;
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

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
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

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
