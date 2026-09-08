package com.example.ecommerce.auth.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
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
        this.email = email != null ? email.trim() : "";
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        if (roles != null && !roles.isEmpty()) {
            Set<String> cleanRoles = new HashSet<>();
            for (String r : roles) {
                if (r != null && !r.trim().isEmpty()) {
                    cleanRoles.add(r.trim().toUpperCase());
                }
            }
            this.roles = Collections.unmodifiableSet(cleanRoles);
        } else {
            this.roles = Collections.emptySet();
        }
    }

    public UserSession(int userId, String email, String firstName, String lastName, Set<String> roles) {
        this(userId, email, firstName, lastName, "", roles);
    }

    public static UserSession fromUser(User user) {
        if (user == null) {
            return null;
        }
        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                if (role != null && role.getRoleName() != null && !role.getRoleName().trim().isEmpty()) {
                    roleNames.add(role.getRoleName().trim().toUpperCase());
                }
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
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        if (first.isEmpty()) {
            return last;
        }
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public Set<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String roleName) {
        if (roleName == null || roles == null) {
            return false;
        }
        return roles.contains(roleName.trim().toUpperCase());
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    public boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public boolean isAuthenticated() {
        return userId > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return userId == that.userId && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", roles=" + roles +
                '}';
    }
}
