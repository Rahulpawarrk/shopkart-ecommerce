package com.example.ecommerce.auth.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model holding user registration details in HTTP session until Email OTP verification succeeds.
 */
public class PendingRegistration implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String emailOtp;
    private String otpHash;
    private LocalDateTime otpExpiry;

    public PendingRegistration() {
    }

    public PendingRegistration(String email, String password, String firstName, String lastName, 
                               String phone, String emailOtp, LocalDateTime otpExpiry) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.emailOtp = emailOtp;
        this.otpExpiry = otpExpiry;
    }

    // Backward-compatible constructor
    public PendingRegistration(String email, String password, String firstName, String lastName, 
                               String phone, String emailOtp, String mobileOtp, LocalDateTime otpExpiry) {
        this(email, password, firstName, lastName, phone, emailOtp, otpExpiry);
    }

    public boolean isOtpExpired() {
        return otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry);
    }

    public boolean isEmailOtpValid(String inputEmailOtp) {
        if (inputEmailOtp == null || otpHash == null) return false;
        if (isOtpExpired()) return false;
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(inputEmailOtp.trim().replaceAll("\\s+", ""), otpHash);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOtpValid(String inputOtp) {
        return isEmailOtpValid(inputOtp);
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public void clearOtp() {
        this.emailOtp = null;
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

    public String getEmailOtp() {
        return emailOtp;
    }

    public void setEmailOtp(String emailOtp) {
        this.emailOtp = emailOtp;
    }

    public String getOtpCode() {
        return emailOtp;
    }

    public void setOtpCode(String otpCode) {
        this.emailOtp = otpCode;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }
}
