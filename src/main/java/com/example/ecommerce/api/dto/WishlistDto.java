package com.example.ecommerce.api.dto;

import java.util.ArrayList;
import java.util.List;

public class WishlistDto {
    private int wishlistId;
    private int userId;
    private List<WishlistItemDto> items = new ArrayList<>();
    private int totalItems;

    public WishlistDto() {}

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

    public List<WishlistItemDto> getItems() {
        return items;
    }

    public void setItems(List<WishlistItemDto> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
