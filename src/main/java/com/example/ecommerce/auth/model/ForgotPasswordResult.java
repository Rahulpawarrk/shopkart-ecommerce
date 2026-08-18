package com.example.ecommerce.auth.model;

import java.io.Serializable;

/**
 * Result DTO for forgot-password requests (supports Email & Mobile Phone SMS OTP).
 */
public class ForgotPasswordResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String method; // "EMAIL" or "PHONE"
    private final String maskedDestination;
    private final String rawDestination;
    private final String resetLink;
    private final String otpCode;
    private final boolean userFound;

    public ForgotPasswordResult(String method, String maskedDestination, String rawDestination, 
                                String resetLink, String otpCode, boolean userFound) {
        this.method = method;
        this.maskedDestination = maskedDestination;
        this.rawDestination = rawDestination;
        this.resetLink = resetLink;
        this.otpCode = otpCode;
        this.userFound = userFound;
    }

    public String getMethod() {
        return method;
    }

    public String getMaskedDestination() {
        return maskedDestination;
    }

    public String getRawDestination() {
        return rawDestination;
    }

    public String getResetLink() {
        return resetLink;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public boolean isUserFound() {
        return userFound;
    }
}
