package com.example.ecommerce.auth.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Lightweight Session DTO stored inside HttpSession upon successful authentication.
 * Avoids keeping password hashes in HTTP session memory.
 */
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final Set<String> roles;

    public UserSession(int userId, String email, String firstName, String lastName, String phone, Set<String> roles) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone != null ? phone : "";
        this.roles = roles != null ? Collections.unmodifiableSet(new HashSet<>(roles)) : Collections.emptySet();
    }

    public UserSession(int userId, String email, String firstName, String lastName, Set<String> roles) {
        this(userId, email, firstName, lastName, "", roles);
    }

    public static UserSession fromUser(User user) {
        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                roleNames.add(role.getRoleName().toUpperCase());
            }
        }
        return new UserSession(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                roleNames
        );
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String roleName) {
        if (roleName == null) return false;
        return roles.contains(roleName.toUpperCase());
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }
}
