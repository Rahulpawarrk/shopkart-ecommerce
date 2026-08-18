package com.example.ecommerce.coupon.service;

import com.example.ecommerce.coupon.dao.CouponDAO;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.model.DiscountType;
import com.example.ecommerce.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService Unit Tests with Mockito")
class CouponServiceTest {

    @Mock
    private CouponDAO couponDAO;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("Should successfully apply percentage coupon capped by maximum discount limit")
    void testApplyPercentageCouponWithMaxCap() {
        Coupon c = new Coupon();
        c.setCode("SAVE20");
        c.setDiscountType(DiscountType.PERCENTAGE);
        c.setDiscountValue(new BigDecimal("20.00")); // 20%
        c.setMinSpend(new BigDecimal("1000.00"));
        c.setMaxDiscount(new BigDecimal("500.00")); // Max cap ₹500
        c.setActive(true);

        when(couponDAO.findByCode("SAVE20")).thenReturn(Optional.of(c));

        // Subtotal ₹5,000 -> 20% is ₹1,000, but capped at ₹500
        Coupon applied = couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("5000.00"));

        assertNotNull(applied);
        assertEquals(new BigDecimal("500.00"), applied.calculateDiscount(new BigDecimal("5000.00")));
    }

    @Test
    @DisplayName("Should successfully apply fixed amount discount")
    void testApplyFixedAmountCoupon() {
        Coupon c = new Coupon();
        c.setCode("FLAT300");
        c.setDiscountType(DiscountType.FIXED_AMOUNT);
        c.setDiscountValue(new BigDecimal("300.00"));
        c.setMinSpend(new BigDecimal("1500.00"));
        c.setActive(true);

        when(couponDAO.findByCode("FLAT300")).thenReturn(Optional.of(c));

        Coupon applied = couponService.validateAndApplyCoupon("FLAT300", new BigDecimal("2000.00"));

        assertNotNull(applied);
        assertEquals(new BigDecimal("300.00"), applied.calculateDiscount(new BigDecimal("2000.00")));
    }

    @Test
    @DisplayName("Should reject coupon when minimum spend is not met")
    void testApplyCouponMinSpendNotMetThrowsException() {
        Coupon c = new Coupon();
        c.setCode("MIN2000");
        c.setDiscountType(DiscountType.PERCENTAGE);
        c.setDiscountValue(new BigDecimal("10.00"));
        c.setMinSpend(new BigDecimal("2000.00"));
        c.setActive(true);

        when(couponDAO.findByCode("MIN2000")).thenReturn(Optional.of(c));

        // Cart subtotal is only ₹1,500
        assertThrows(ValidationException.class, () ->
            couponService.validateAndApplyCoupon("MIN2000", new BigDecimal("1500.00"))
        );
    }

    @Test
    @DisplayName("Should reject coupon when campaign date is expired")
    void testApplyExpiredCouponThrowsException() {
        Coupon c = new Coupon();
        c.setCode("EXPIRED2025");
        c.setDiscountType(DiscountType.PERCENTAGE);
        c.setDiscountValue(new BigDecimal("15.00"));
        c.setStartDate(LocalDateTime.now().minusDays(30));
        c.setEndDate(LocalDateTime.now().minusDays(5)); // Expired 5 days ago
        c.setActive(true);

        when(couponDAO.findByCode("EXPIRED2025")).thenReturn(Optional.of(c));

        assertThrows(ValidationException.class, () ->
            couponService.validateAndApplyCoupon("EXPIRED2025", new BigDecimal("3000.00"))
        );
    }

    @Test
    @DisplayName("Should reject coupon when total global usage limit is reached")
    void testApplyUsageLimitExceededThrowsException() {
        Coupon c = new Coupon();
        c.setCode("LIMITED100");
        c.setDiscountType(DiscountType.FIXED_AMOUNT);
        c.setDiscountValue(new BigDecimal("100.00"));
        c.setUsageLimit(100);
        c.setUsedCount(100); // 100/100 used
        c.setActive(true);

        when(couponDAO.findByCode("LIMITED100")).thenReturn(Optional.of(c));

        assertThrows(ValidationException.class, () ->
            couponService.validateAndApplyCoupon("LIMITED100", new BigDecimal("1000.00"))
        );
    }
}
