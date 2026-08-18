package com.example.ecommerce.auth.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model holding user registration details in HTTP session until email OTP verification succeeds.
 */
public class PendingRegistration implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String otpCode;
    private LocalDateTime otpExpiry;

    public PendingRegistration() {
    }

    public PendingRegistration(String email, String password, String firstName, String lastName, 
                               String phone, String otpCode, LocalDateTime otpExpiry) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.otpCode = otpCode;
        this.otpExpiry = otpExpiry;
    }

    public boolean isOtpExpired() {
        return otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry);
    }

    public boolean isOtpValid(String inputOtp) {
        if (inputOtp == null || otpCode == null) return false;
        return !isOtpExpired() && otpCode.trim().equals(inputOtp.trim());
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }
}
