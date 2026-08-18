package com.example.ecommerce.review.model;

import java.io.Serializable;

/**
 * Aggregated rating metrics for a product.
 */
public class RatingSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int productId;
    private double averageRating = 0.0;
    private int totalReviews = 0;
    private int fiveStarCount = 0;
    private int fourStarCount = 0;
    private int threeStarCount = 0;
    private int twoStarCount = 0;
    private int oneStarCount = 0;

    public RatingSummary() {}

    public RatingSummary(int productId, double averageRating, int totalReviews) {
        this.productId = productId;
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
        this.totalReviews = totalReviews;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getFiveStarCount() {
        return fiveStarCount;
    }

    public void setFiveStarCount(int fiveStarCount) {
        this.fiveStarCount = fiveStarCount;
    }

    public int getFourStarCount() {
        return fourStarCount;
    }

    public void setFourStarCount(int fourStarCount) {
        this.fourStarCount = fourStarCount;
    }

    public int getThreeStarCount() {
        return threeStarCount;
    }

    public void setThreeStarCount(int threeStarCount) {
        this.threeStarCount = threeStarCount;
    }

    public int getTwoStarCount() {
        return twoStarCount;
    }

    public void setTwoStarCount(int twoStarCount) {
        this.twoStarCount = twoStarCount;
    }

    public int getOneStarCount() {
        return oneStarCount;
    }

    public void setOneStarCount(int oneStarCount) {
        this.oneStarCount = oneStarCount;
    }

    public int getPercentage(int count) {
        if (totalReviews <= 0) return 0;
        return (int) Math.round(((double) count / totalReviews) * 100.0);
    }

    public int getFiveStarPercentage() {
        return getPercentage(fiveStarCount);
    }

    public int getFourStarPercentage() {
        return getPercentage(fourStarCount);
    }

    public int getThreeStarPercentage() {
        return getPercentage(threeStarCount);
    }

    public int getTwoStarPercentage() {
        return getPercentage(twoStarCount);
    }

    public int getOneStarPercentage() {
        return getPercentage(oneStarCount);
    }

    public String getStarsDisplay() {
        int rounded = (int) Math.round(averageRating);
        rounded = Math.max(0, Math.min(5, rounded));
        return "★".repeat(rounded) + "☆".repeat(5 - rounded);
    }

    public String getFormattedAverage() {
        return String.format(java.util.Locale.US, "%.1f", averageRating);
    }
}
