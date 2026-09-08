package com.example.ecommerce.api.dto;

import com.example.ecommerce.auth.model.UserSession;
import java.util.Set;

public class UserDto {
    private int userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private Set<String> roles;
    private boolean isAdmin;

    public UserDto() {}

    public static UserDto fromSession(UserSession session) {
        if (session == null) return null;
        UserDto dto = new UserDto();
        dto.setUserId(session.getUserId());
        dto.setEmail(session.getEmail());
        dto.setFirstName(session.getFirstName());
        dto.setLastName(session.getLastName());
        dto.setFullName(session.getFullName());
        dto.setPhone(session.getPhone());
        dto.setRoles(session.getRoles());
        dto.setAdmin(session.isAdmin());
        return dto;
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
