package com.example.ecommerce.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class AdminReturnStatusUpdateRequest {

    @NotBlank(message = "Return status is required (APPROVED, REJECTED, REFUNDED)")
    private String status;

    private String adminNotes;

    private BigDecimal refundAmount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
