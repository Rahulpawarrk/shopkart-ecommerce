package com.example.ecommerce.api.dto;

import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.coupon.model.Coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {
    private int cartId;
    private int userId;
    private List<CartItemDto> items = new ArrayList<>();
    private int totalQuantity;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal couponDiscount = BigDecimal.ZERO;
    private String appliedCouponCode;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal finalTotal;
    private boolean empty;

    public CartDto() {}

    public static CartDto fromEntity(Cart cart, Coupon appliedCoupon) {
        if (cart == null) return new CartDto();
        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());
        dto.setUserId(cart.getUserId());
        dto.setTotalQuantity(cart.getTotalQuantity());
        dto.setSubtotal(cart.getSubtotal());
        dto.setTotalDiscount(cart.getTotalDiscount());
        dto.setTaxAmount(cart.getEstimatedTax());
        dto.setShippingAmount(BigDecimal.ZERO);
        dto.setEmpty(cart.isEmpty());

        BigDecimal total = cart.getGrandTotal();
        if (appliedCoupon != null && !cart.isEmpty()) {
            BigDecimal cDiscount = appliedCoupon.calculateDiscount(total);
            dto.setCouponDiscount(cDiscount);
            dto.setAppliedCouponCode(appliedCoupon.getCode());
            total = total.subtract(cDiscount).max(BigDecimal.ZERO);
        }
        dto.setFinalTotal(total);

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                dto.getItems().add(CartItemDto.fromEntity(item));
            }
        }
        return dto;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public void setAppliedCouponCode(String appliedCouponCode) {
        this.appliedCouponCode = appliedCouponCode;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
