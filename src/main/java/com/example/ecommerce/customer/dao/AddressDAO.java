package com.example.ecommerce.customer.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.customer.model.Address;
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

/**
 * Data Access Object for customer delivery and billing addresses in Microsoft SQL Server.
 */
public class AddressDAO {

    private static final Logger logger = LoggerFactory.getLogger(AddressDAO.class);

    /**
     * Retrieves all saved addresses for a specific customer.
     */
    public List<Address> findByUserId(int userId) {
        String sql = "SELECT address_id, user_id, address_type, full_name, phone, address_line1, " +
                     "address_line2, city, state, postal_code, country, is_default, created_at, updated_at " +
                     "FROM dbo.addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        List<Address> addresses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving addresses for userId: {}", userId, e);
            throw new DatabaseException("Error retrieving user addresses", e);
        }
        return addresses;
    }

    /**
     * Retrieves address by its primary key ID.
     */
    public Optional<Address> findById(int addressId) {
        String sql = "SELECT address_id, user_id, address_type, full_name, phone, address_line1, " +
                     "address_line2, city, state, postal_code, country, is_default, created_at, updated_at " +
                     "FROM dbo.addresses WHERE address_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding address by ID: {}", addressId, e);
            throw new DatabaseException("Error retrieving address by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds default shipping address for a user.
     */
    public Optional<Address> findDefaultByUserId(int userId) {
        String sql = "SELECT TOP 1 address_id, user_id, address_type, full_name, phone, address_line1, " +
                     "address_line2, city, state, postal_code, country, is_default, created_at, updated_at " +
                     "FROM dbo.addresses WHERE user_id = ? AND is_default = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAddress(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding default address for userId: {}", userId, e);
            throw new DatabaseException("Error retrieving default address", e);
        }
        return Optional.empty();
    }

    /**
     * Counts how many addresses the user has saved.
     */
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM dbo.addresses WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting addresses for userId: {}", userId, e);
        }
        return 0;
    }

    /**
     * Creates a new address record.
     */
    public int createAddress(Address address) {
        String sql = "INSERT INTO dbo.addresses (user_id, address_type, full_name, phone, address_line1, " +
                     "address_line2, city, state, postal_code, country, is_default, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, address.getUserId());
            stmt.setString(2, address.getAddressType());
            stmt.setString(3, address.getFullName().trim());
            stmt.setString(4, address.getPhone().trim());
            stmt.setString(5, address.getAddressLine1().trim());
            stmt.setString(6, address.getAddressLine2() != null ? address.getAddressLine2().trim() : null);
            stmt.setString(7, address.getCity().trim());
            stmt.setString(8, address.getState().trim());
            stmt.setString(9, address.getPostalCode().trim());
            stmt.setString(10, address.getCountry().trim());
            stmt.setBoolean(11, address.isDefaultAddress());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    address.setAddressId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to create address, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error creating address for userId: {}", address.getUserId(), e);
            throw new DatabaseException("Failed to save address", e);
        }
    }

    /**
     * Updates an existing address (enforcing user ownership).
     */
    public boolean updateAddress(Address address) {
        String sql = "UPDATE dbo.addresses SET address_type = ?, full_name = ?, phone = ?, " +
                     "address_line1 = ?, address_line2 = ?, city = ?, state = ?, postal_code = ?, " +
                     "country = ?, updated_at = SYSDATETIME() WHERE address_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, address.getAddressType());
            stmt.setString(2, address.getFullName().trim());
            stmt.setString(3, address.getPhone().trim());
            stmt.setString(4, address.getAddressLine1().trim());
            stmt.setString(5, address.getAddressLine2() != null ? address.getAddressLine2().trim() : null);
            stmt.setString(6, address.getCity().trim());
            stmt.setString(7, address.getState().trim());
            stmt.setString(8, address.getPostalCode().trim());
            stmt.setString(9, address.getCountry().trim());
            stmt.setInt(10, address.getAddressId());
            stmt.setInt(11, address.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating address ID: {}", address.getAddressId(), e);
            throw new DatabaseException("Failed to update address", e);
        }
    }

    /**
     * Deletes an address (enforcing user ownership).
     */
    public boolean deleteAddress(int addressId, int userId) {
        String sql = "DELETE FROM dbo.addresses WHERE address_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting address ID: {} for userId: {}", addressId, userId, e);
            throw new DatabaseException("Failed to delete address", e);
        }
    }

    /**
     * Atomically designates one address as default while unsetting all other addresses for the user.
     */
    public void setDefaultAddress(int addressId, int userId) {
        String clearSql = "UPDATE dbo.addresses SET is_default = 0 WHERE user_id = ?";
        String setSql = "UPDATE dbo.addresses SET is_default = 1 WHERE address_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                try (PreparedStatement clearStmt = conn.prepareStatement(clearSql)) {
                    clearStmt.setInt(1, userId);
                    clearStmt.executeUpdate();
                }
                try (PreparedStatement setStmt = conn.prepareStatement(setSql)) {
                    setStmt.setInt(1, addressId);
                    setStmt.setInt(2, userId);
                    setStmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error setting default address ID: {} for userId: {}", addressId, userId, e);
            throw new DatabaseException("Failed to set default address", e);
        }
    }

    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setAddressId(rs.getInt("address_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setAddressType(rs.getString("address_type"));
        a.setFullName(rs.getString("full_name"));
        a.setPhone(rs.getString("phone"));
        a.setAddressLine1(rs.getString("address_line1"));
        a.setAddressLine2(rs.getString("address_line2"));
        a.setCity(rs.getString("city"));
        a.setState(rs.getString("state"));
        a.setPostalCode(rs.getString("postal_code"));
        a.setCountry(rs.getString("country"));
        a.setDefaultAddress(rs.getBoolean("is_default"));
        a.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        a.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return a;
    }
}
