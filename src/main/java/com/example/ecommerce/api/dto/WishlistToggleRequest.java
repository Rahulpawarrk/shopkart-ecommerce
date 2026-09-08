package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotNull;

public class WishlistToggleRequest {

    @NotNull(message = "Product ID is required")
    private Integer productId;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
