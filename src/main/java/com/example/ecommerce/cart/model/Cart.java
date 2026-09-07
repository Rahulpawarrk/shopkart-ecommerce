package com.example.ecommerce.cart.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customer Shopping Cart Entity aggregating line items and calculating authoritative totals.
 */
@Entity
@Table(name = "carts", schema = "dbo")
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int cartId;

    @Column(name = "user_id", nullable = false, unique = true)
    private int userId;

    @Transient
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Cart() {}

    public Cart(int cartId, int userId) {
        this.cartId = cartId;
        this.userId = userId;
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

    public List<CartItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
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

    // Financial Aggregations
    public BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : getItems()) {
            subtotal = subtotal.add(item.getLineSubtotal());
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalDiscount() {
        BigDecimal discount = BigDecimal.ZERO;
        for (CartItem item : getItems()) {
            discount = discount.add(item.getDiscountAmount().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscountedSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : getItems()) {
            total = total.add(item.getLineTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getEstimatedTax() {
        BigDecimal tax = BigDecimal.ZERO;
        for (CartItem item : getItems()) {
            tax = tax.add(item.getLineTax());
        }
        return tax.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getGrandTotal() {
        BigDecimal total = getDiscountedSubtotal().subtract(getCouponDiscount()).add(getEstimatedTax());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : getItems()) {
            total += item.getQuantity();
        }
        return total;
    }

    private Integer couponId;
    private String appliedCouponCode;
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode != null ? appliedCouponCode : "";
    }

    public void setAppliedCouponCode(String appliedCouponCode) {
        this.appliedCouponCode = appliedCouponCode;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount != null ? couponDiscount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount != null ? couponDiscount : BigDecimal.ZERO;
    }

    public BigDecimal getTotalSavings() {
        return getTotalDiscount().add(getCouponDiscount()).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isEmpty() {
        return getItems().isEmpty();
    }

    public boolean hasUnavailableItems() {
        for (CartItem item : getItems()) {
            if (!item.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public static Cart createDirectBuyCart(int userId, com.example.ecommerce.product.model.Product product, int quantity) {
        Cart directCart = new Cart(0, userId);
        directCart.addItem(CartItem.fromProduct(product, quantity));
        return directCart;
    }
}
