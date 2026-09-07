package com.example.ecommerce.wishlist.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customer Wishlist Entity aggregating saved products.
 */
@Entity
@Table(name = "wishlists", schema = "dbo")
public class Wishlist implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private int wishlistId;

    @Column(name = "user_id", nullable = false, unique = true)
    private int userId;

    @Transient
    private List<WishlistItem> items = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Wishlist() {}

    public Wishlist(int wishlistId, int userId) {
        this.wishlistId = wishlistId;
        this.userId = userId;
    }

    public int getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(int wishlistId) {
        this.wishlistId = wishlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<WishlistItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }

    public void setItems(List<WishlistItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(WishlistItem item) {
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

    public boolean isEmpty() {
        return getItems().isEmpty();
    }

    public int getTotalItems() {
        return getItems().size();
    }
}
