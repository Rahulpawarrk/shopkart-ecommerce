package com.example.ecommerce.review.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Product Review & Rating Entity.
 */
@Entity
@Table(name = "reviews", schema = "dbo")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId;

    @Column(name = "product_id", nullable = false)
    private int productId;

    @Transient
    private String productName;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Transient
    private String customerName;

    @Transient
    private String customerEmail;

    @Column(name = "rating", nullable = false)
    private int rating = 5; // 1 to 5

    @Column(name = "review_title", nullable = false)
    private String title;

    @Column(name = "review_text", nullable = false)
    private String comment;

    @Column(name = "image_url")
    private String imageUrl;

    @Transient
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    @Transient
    private boolean verifiedPurchase = false;

    @Transient
    private boolean approved = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Review() {}

    public Review(int productId, int userId, int rating, String title, String comment, boolean verifiedPurchase) {
        this(productId, userId, rating, title, comment, null, verifiedPurchase);
    }

    public Review(int productId, int userId, int rating, String title, String comment, String imageUrl, boolean verifiedPurchase) {
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.imageUrl = imageUrl;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrls.add(imageUrl);
        }
        this.verifiedPurchase = verifiedPurchase;
        this.approved = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = Math.max(1, Math.min(5, rating));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl != null && !imageUrl.trim().isEmpty() && !this.imageUrls.contains(imageUrl)) {
            this.imageUrls.add(imageUrl);
        }
    }

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new java.util.ArrayList<>();
        if (this.imageUrl == null && !this.imageUrls.isEmpty()) {
            this.imageUrl = this.imageUrls.get(0);
        }
    }

    public boolean hasImages() {
        return (imageUrl != null && !imageUrl.trim().isEmpty()) || (imageUrls != null && !imageUrls.isEmpty());
    }

    public boolean isVerifiedPurchase() {
        return verifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
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

    public String getStarsDisplay() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    public String getReviewTitle() {
        return title;
    }

    public String getReviewText() {
        return comment;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null) return "";
        return updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public boolean isEdited() {
        return updatedAt != null && createdAt != null && updatedAt.isAfter(createdAt.plusSeconds(5));
    }
}
