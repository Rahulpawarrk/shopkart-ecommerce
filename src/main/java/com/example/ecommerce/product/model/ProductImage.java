package com.example.ecommerce.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Product Image Model representing gallery images for a catalog product.
 */
public class ProductImage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int imageId;
    private int productId;
    private String imageUrl;
    private String altText;
    private int displayOrder;
    private boolean primary;
    private LocalDateTime createdAt;

    public ProductImage() {}

    public ProductImage(String imageUrl, String altText, int displayOrder, boolean primary) {
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.primary = primary;
    }

    public ProductImage(int imageId, int productId, String imageUrl, String altText, int displayOrder, boolean primary) {
        this.imageId = imageId;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.primary = primary;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
