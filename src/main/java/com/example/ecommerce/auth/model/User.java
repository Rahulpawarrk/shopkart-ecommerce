package com.example.ecommerce.auth.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity representing a customer or administrative user.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private String status; // 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Role> roles = new ArrayList<>();

    public User() {}

    public User(int userId, String email, String passwordHash, String firstName, String lastName, String phone, String status) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean hasRole(String roleName) {
        if (roles == null || roleName == null) return false;
        return roles.stream().anyMatch(r -> roleName.equalsIgnoreCase(r.getRoleName()));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }
}
