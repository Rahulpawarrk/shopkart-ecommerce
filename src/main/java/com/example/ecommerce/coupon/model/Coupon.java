package com.example.ecommerce.coupon.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Promotional Coupon Entity supporting Percentage discounts with maximum caps
 * and Fixed Amount flat discounts.
 */
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private int couponId;
    private String code;
    private String description;
    private DiscountType discountType = DiscountType.PERCENTAGE;
    private BigDecimal discountValue = BigDecimal.ZERO;
    private BigDecimal minSpend = BigDecimal.ZERO;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private int usedCount = 0;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Coupon() {}

    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim().toUpperCase() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue != null ? discountValue : BigDecimal.ZERO;
    }

    public BigDecimal getMinSpend() {
        return minSpend;
    }

    public void setMinSpend(BigDecimal minSpend) {
        this.minSpend = minSpend != null ? minSpend : BigDecimal.ZERO;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    /**
     * Calculates authoritative discount amount for a given order subtotal.
     */
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        if (minSpend != null && subtotal.compareTo(minSpend) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal calculated;
        if (discountType == DiscountType.PERCENTAGE) {
            calculated = subtotal.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDiscount != null && maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
                calculated = calculated.min(maxDiscount);
            }
        } else {
            calculated = discountValue.min(subtotal);
        }

        return calculated.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) return true;
        if (endDate != null && now.isAfter(endDate)) return true;
        return false;
    }

    public boolean isUsageLimitReached() {
        return usageLimit != null && usedCount >= usageLimit;
    }

    public boolean isValidNow() {
        return active && !isExpired() && !isUsageLimitReached();
    }
}
