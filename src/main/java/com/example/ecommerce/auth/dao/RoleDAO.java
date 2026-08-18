package com.example.ecommerce.auth.dao;

import com.example.ecommerce.auth.model.Role;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO handling roles and role-assignment queries in SQL Server.
 */
public class RoleDAO {

    private static final Logger logger = LoggerFactory.getLogger(RoleDAO.class);

    /**
     * Finds a role by its unique role name.
     */
    public Optional<Role> findByName(String roleName) {
        String sql = "SELECT role_id, role_name, description, created_at FROM dbo.roles WHERE role_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRole(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding role by name: {}", roleName, e);
            throw new DatabaseException("Error finding role by name", e);
        }
        return Optional.empty();
    }

    /**
     * Assigns a role to a user within an existing database transaction connection.
     */
    public void assignRoleToUser(int userId, int roleId, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.user_roles (user_id, role_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, roleId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves all roles assigned to a specific user.
     */
    public List<Role> getRolesByUserId(int userId) {
        String sql = "SELECT r.role_id, r.role_name, r.description, r.created_at " +
                     "FROM dbo.roles r " +
                     "INNER JOIN dbo.user_roles ur ON r.role_id = ur.role_id " +
                     "WHERE ur.user_id = ?";
        List<Role> roles = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapResultSetToRole(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching roles for userId: {}", userId, e);
            throw new DatabaseException("Error fetching user roles", e);
        }
        return roles;
    }

    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getInt("role_id"));
        role.setRoleName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        role.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return role;
    }
}
