package com.example.ecommerce.coupon.service;

import com.example.ecommerce.coupon.dao.CouponDAO;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.model.DiscountType;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Layer enforcing promotional discount rules, validity dates, minimum spend thresholds,
 * and usage caps.
 */
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    private final CouponDAO couponDAO;

    public CouponService() {
        this.couponDAO = new CouponDAO();
    }

    public CouponService(CouponDAO couponDAO) {
        this.couponDAO = couponDAO;
    }

    /**
     * Validates and applies a coupon code against the customer's current cart subtotal.
     */
    public Coupon validateAndApplyCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Please enter a promotional coupon code.");
        }

        Coupon coupon = couponDAO.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon code '" + code.trim().toUpperCase() + "' is invalid."));

        if (!coupon.isActive()) {
            throw new ValidationException("Coupon code '" + coupon.getCode() + "' is no longer active.");
        }

        if (coupon.isExpired()) {
            throw new ValidationException("Coupon code '" + coupon.getCode() + "' has expired or is not yet valid.");
        }

        if (coupon.isUsageLimitReached()) {
            throw new ValidationException("Coupon code '" + coupon.getCode() + "' has reached its maximum global redemptions.");
        }

        if (coupon.getMinSpend() != null && subtotal.compareTo(coupon.getMinSpend()) < 0) {
            throw new ValidationException("Minimum cart subtotal of ₹" + coupon.getMinSpend() + " is required to use code '" + coupon.getCode() + "'.");
        }

        BigDecimal discount = coupon.calculateDiscount(subtotal);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Coupon does not provide any discount for the current order items.");
        }

        logger.info("Coupon [{}] successfully validated with discount [₹{}] for subtotal [₹{}]", coupon.getCode(), discount, subtotal);
        return coupon;
    }

    public Optional<Coupon> getCouponByCode(String code) {
        return couponDAO.findByCode(code);
    }

    public Coupon getCouponById(int couponId) {
        return couponDAO.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));
    }

    public int createCoupon(Coupon coupon) {
        validateCouponData(coupon);

        if (couponDAO.findByCode(coupon.getCode()).isPresent()) {
            throw new ValidationException("A coupon with code '" + coupon.getCode() + "' already exists.");
        }

        int id = couponDAO.createCoupon(coupon);
        logger.info("Created new promotional coupon [id={}, code={}]", id, coupon.getCode());
        return id;
    }

    public void updateCoupon(Coupon coupon) {
        validateCouponData(coupon);

        Optional<Coupon> existing = couponDAO.findByCode(coupon.getCode());
        if (existing.isPresent() && existing.get().getCouponId() != coupon.getCouponId()) {
            throw new ValidationException("Coupon code '" + coupon.getCode() + "' is already assigned to another coupon.");
        }

        couponDAO.updateCoupon(coupon);
        logger.info("Updated coupon [id={}, code={}]", coupon.getCouponId(), coupon.getCode());
    }

    public void toggleCouponStatus(int couponId, boolean active) {
        couponDAO.setActiveStatus(couponId, active);
        logger.info("Toggled coupon [id={}] active status to: {}", couponId, active);
    }

    public void deleteCoupon(int couponId) {
        couponDAO.deleteCoupon(couponId);
        logger.info("Deleted coupon [id={}]", couponId);
    }

    public Pagination<Coupon> getAllCoupons(String keyword, int page, int pageSize) {
        return couponDAO.findAll(keyword, page, pageSize);
    }

    public Map<String, Object> getCouponSummaryStats() {
        return couponDAO.getCouponSummaryStats();
    }

    private void validateCouponData(Coupon coupon) {
        List<String> errors = new ArrayList<>();
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            errors.add("Coupon code is required.");
        }
        if (coupon.getDiscountType() == null) {
            errors.add("Discount type (PERCENTAGE or FIXED_AMOUNT) is required.");
        }
        if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Discount value must be greater than zero.");
        }
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE && coupon.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            errors.add("Percentage discount cannot exceed 100%.");
        }
        if (coupon.getStartDate() != null && coupon.getEndDate() != null && coupon.getStartDate().isAfter(coupon.getEndDate())) {
            errors.add("Start date cannot be after end date.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
