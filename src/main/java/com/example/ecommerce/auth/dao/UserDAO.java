package com.example.ecommerce.auth.dao;

import com.example.ecommerce.auth.model.User;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Data Access Object for User operations against Microsoft SQL Server.
 */
@Repository
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final RoleDAO roleDAO;

    public UserDAO() {
        this.roleDAO = new RoleDAO();
    }

    public UserDAO(RoleDAO roleDAO) {
        this.roleDAO = roleDAO;
    }

    /**
     * Checks if a user already exists with the given email address.
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM dbo.users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking email existence: {}", email, e);
            throw new DatabaseException("Error checking email existence", e);
        }
    }

    /**
     * Checks if a user already exists with the given phone/mobile number.
     */
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM dbo.users WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking phone existence: {}", phone, e);
            throw new DatabaseException("Error checking phone existence", e);
        }
    }

    /**
     * Finds a user by phone/mobile number and loads their assigned roles.
     */
    public Optional<User> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, phone, status, created_at, updated_at " +
                     "FROM dbo.users WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    user.setRoles(roleDAO.getRolesByUserId(user.getUserId()));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by phone: {}", phone, e);
            throw new DatabaseException("Error retrieving user by phone", e);
        }
        return Optional.empty();
    }

    /**
     * Inserts a new user record into dbo.users within an active transaction connection.
     *
     * @return generated user_id
     */
    public int createUser(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.users (email, password_hash, first_name, last_name, phone, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail().trim().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFirstName().trim());
            stmt.setString(4, user.getLastName().trim());
            stmt.setString(5, user.getPhone() != null ? user.getPhone().trim() : null);
            stmt.setString(6, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * Provisions initial Cart and Wishlist records for a newly created user in the same transaction.
     */
    public void provisionCartAndWishlist(int userId, Connection conn) throws SQLException {
        String cartSql = "INSERT INTO dbo.carts (user_id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(cartSql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }

        String wishlistSql = "INSERT INTO dbo.wishlists (user_id, created_at) VALUES (?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(wishlistSql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Finds a user by email address and loads their assigned roles.
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, phone, status, created_at, updated_at " +
                     "FROM dbo.users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    user.setRoles(roleDAO.getRolesByUserId(user.getUserId()));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseException("Error retrieving user by email", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their primary key user_id.
     */
    public Optional<User> findById(int userId) {
        String sql = "SELECT user_id, email, password_hash, first_name, last_name, phone, status, created_at, updated_at " +
                     "FROM dbo.users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    user.setRoles(roleDAO.getRolesByUserId(user.getUserId()));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", userId, e);
            throw new DatabaseException("Error retrieving user by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Updates user's first name, last name, and phone.
     */
    public boolean updateProfile(int userId, String firstName, String lastName, String phone) {
        String sql = "UPDATE dbo.users SET first_name = ?, last_name = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName.trim());
            stmt.setString(2, lastName.trim());
            stmt.setString(3, phone != null ? phone.trim() : null);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating profile for userId: {}", userId, e);
            throw new DatabaseException("Error updating user profile", e);
        }
    }

    /**
     * Updates the BCrypt password hash for a user.
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE dbo.users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for userId: {}", userId, e);
            throw new DatabaseException("Error updating user password", e);
        }
    }

    /**
     * Retrieves all administrator accounts (users with ADMIN / ROLE_ADMIN role).
     */
    public List<User> findAllAdmins() {
        String sql = "SELECT DISTINCT u.user_id, u.email, u.password_hash, u.first_name, u.last_name, u.phone, u.status, u.created_at, u.updated_at " +
                     "FROM dbo.users u " +
                     "INNER JOIN dbo.user_roles ur ON u.user_id = ur.user_id " +
                     "INNER JOIN dbo.roles r ON ur.role_id = r.role_id " +
                     "WHERE UPPER(r.role_name) IN ('ADMIN', 'ROLE_ADMIN') " +
                     "ORDER BY u.created_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                user.setRoles(roleDAO.getRolesByUserId(user.getUserId()));
                list.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving admin users", e);
            throw new DatabaseException("Error retrieving admin accounts", e);
        }
        return list;
    }

    /**
     * Atomically creates a new Administrator account with strictly the ADMIN role.
     * Note: Admins are not provisioned with shopping carts or customer order capabilities.
     *
     * @param adminUser User model populated with first_name, last_name, email, phone, passwordHash
     * @return generated user_id
     */
    public int createAdminUser(User adminUser) {
        String insertUserSql = "INSERT INTO dbo.users (email, password_hash, first_name, last_name, phone, status, created_at, updated_at) " +
                               "VALUES (?, ?, ?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        String findRoleSql = "SELECT role_id FROM dbo.roles WHERE UPPER(role_name) = 'ADMIN' OR UPPER(role_name) = 'ROLE_ADMIN'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int generatedUserId;
                try (PreparedStatement stmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, adminUser.getEmail().trim().toLowerCase());
                    stmt.setString(2, adminUser.getPasswordHash());
                    stmt.setString(3, adminUser.getFirstName().trim());
                    stmt.setString(4, adminUser.getLastName().trim());
                    stmt.setString(5, adminUser.getPhone() != null ? adminUser.getPhone().trim() : null);

                    int affected = stmt.executeUpdate();
                    if (affected == 0) throw new SQLException("Failed to insert admin user record.");

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedUserId = keys.getInt(1);
                            adminUser.setUserId(generatedUserId);
                        } else {
                            throw new SQLException("Failed to obtain generated user_id for admin.");
                        }
                    }
                }

                // Resolve Admin Role ID
                int adminRoleId = 1; // Default
                try (PreparedStatement rStmt = conn.prepareStatement(findRoleSql);
                     ResultSet rRs = rStmt.executeQuery()) {
                    if (rRs.next()) {
                        adminRoleId = rRs.getInt(1);
                    }
                }

                // Assign ONLY the Admin Role (NO customer roles, NO cart/wishlist)
                roleDAO.assignRoleToUser(generatedUserId, adminRoleId, conn);

                conn.commit();
                logger.info("Admin account successfully created with userId: {}", generatedUserId);
                return generatedUserId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error creating admin user: {}", adminUser.getEmail(), e);
            throw new DatabaseException("Error creating admin user account", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email") != null ? rs.getString("email").trim() : null);
        String hash = rs.getString("password_hash");
        user.setPasswordHash(hash != null ? hash.trim() : null);
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getString("status") != null ? rs.getString("status").trim() : null);
        user.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        user.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return user;
    }
}
