package com.example.ecommerce.customer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Customer Postal Address Entity for shipping and billing fulfillment.
 */
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    private int addressId;
    private int userId;
    private String addressType = "SHIPPING"; // 'SHIPPING', 'BILLING', 'BOTH'
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country = "India";
    private boolean defaultAddress = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Address() {}

    public Address(int addressId, int userId, String fullName, String phone, String addressLine1, 
                   String addressLine2, String city, String state, String postalCode, 
                   String country, boolean defaultAddress) {
        this.addressId = addressId;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.defaultAddress = defaultAddress;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public boolean isDefault() {
        return defaultAddress;
    }

    public boolean getIsDefault() {
        return defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public void setDefault(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public void setIsDefault(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
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

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" - ").append(postalCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }
}
