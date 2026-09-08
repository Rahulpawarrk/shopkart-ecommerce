package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
