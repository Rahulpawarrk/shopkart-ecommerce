package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentFailureRequest {

    @NotNull(message = "Order ID is required")
    private Integer orderId;

    private String reason;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
