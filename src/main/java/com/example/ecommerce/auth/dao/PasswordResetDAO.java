package com.example.ecommerce.auth.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Data Access Object for password reset token lifecycle management.
 * Hashes all tokens and OTPs using SHA-256 prior to database persistence.
 */
@Repository
public class PasswordResetDAO {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetDAO.class);

    private String hashToken(String raw) {
        if (raw == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    /**
     * Stores a new password reset token for a user with a 1-hour expiry.
     * Deletes any existing unused tokens for the same user first.
     */
    public void createToken(int userId, String token) {
        String deleteSql = "DELETE FROM dbo.password_reset_tokens WHERE user_id = ? AND (is_used = false OR is_used = 0)";
        String insertSql = "INSERT INTO dbo.password_reset_tokens (user_id, token, expires_at, is_used, created_at) " +
                           "VALUES (?, ?, ?, false, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                    delStmt.setInt(1, userId);
                    delStmt.executeUpdate();
                }
                try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                    insStmt.setInt(1, userId);
                    insStmt.setString(2, hashToken(token));
                    // 1-hour expiry stored in local time
                    insStmt.setObject(3, LocalDateTime.now().plusHours(1));
                    insStmt.executeUpdate();
                }
                conn.commit();
                logger.debug("Password reset token created for userId: {}", userId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error creating password reset token for userId: {}", userId, e);
            throw new DatabaseException("Failed to create password reset token", e);
        }
    }

    /**
     * Stores a new 6-digit OTP code for a user with a 10-minute expiry.
     * Deletes any existing unused tokens/OTPs for the same user first.
     */
    public void createOtp(int userId, String otpCode) {
        String deleteSql = "DELETE FROM dbo.password_reset_tokens WHERE user_id = ? AND (is_used = false OR is_used = 0)";
        String insertSql = "INSERT INTO dbo.password_reset_tokens (user_id, token, expires_at, is_used, created_at) " +
                           "VALUES (?, ?, ?, false, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                    delStmt.setInt(1, userId);
                    delStmt.executeUpdate();
                }
                try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                    insStmt.setInt(1, userId);
                    insStmt.setString(2, hashToken(otpCode));
                    // 10-minute expiry for SMS OTPs
                    insStmt.setObject(3, LocalDateTime.now().plusMinutes(10));
                    insStmt.executeUpdate();
                }
                conn.commit();
                logger.debug("Password reset OTP created for userId: {}", userId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error creating password reset OTP for userId: {}", userId, e);
            throw new DatabaseException("Failed to create password reset OTP", e);
        }
    }

    /**
     * Validates a 6-digit OTP code for a specific user.
     */
    public boolean validateOtp(int userId, String otpCode) {
        String sql = "SELECT token_id FROM dbo.password_reset_tokens " +
                     "WHERE user_id = ? AND token = ? AND (is_used = false OR is_used = 0) AND expires_at > CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, hashToken(otpCode));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error validating OTP for userId: {}", userId, e);
            throw new DatabaseException("Error validating reset OTP", e);
        }
    }

    /**
     * Finds a valid (not used, not expired) token and returns the user_id it belongs to.
     */
    public Optional<Integer> findValidToken(String token) {
        String sql = "SELECT user_id FROM dbo.password_reset_tokens " +
                     "WHERE token = ? AND (is_used = false OR is_used = 0) AND expires_at > CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashToken(token));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding password reset token: {}", token, e);
            throw new DatabaseException("Error validating reset token", e);
        }
        return Optional.empty();
    }

    /**
     * Marks a token as used so it cannot be replayed.
     */
    public void invalidateToken(String token) {
        String sql = "UPDATE dbo.password_reset_tokens SET is_used = true WHERE token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashToken(token));
            stmt.executeUpdate();
            logger.debug("Password reset token invalidated.");
        } catch (SQLException e) {
            logger.error("Error invalidating password reset token", e);
            throw new DatabaseException("Error invalidating reset token", e);
        }
    }

    /**
     * Marks a token as used scoped to a specific user.
     */
    public void invalidateToken(int userId, String token) {
        String sql = "UPDATE dbo.password_reset_tokens SET is_used = true WHERE user_id = ? AND token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, hashToken(token));
            stmt.executeUpdate();
            logger.debug("Password reset token invalidated for userId: {}", userId);
        } catch (SQLException e) {
            logger.error("Error invalidating password reset token for userId: {}", userId, e);
            throw new DatabaseException("Error invalidating reset token", e);
        }
    }

    /**
     * Invalidates all reset tokens for a user upon successful password reset.
     */
    public void invalidateAllUserTokens(int userId) {
        String sql = "UPDATE dbo.password_reset_tokens SET is_used = true WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.debug("All password reset tokens invalidated for userId: {}", userId);
        } catch (SQLException e) {
            logger.error("Error invalidating all reset tokens for userId: {}", userId, e);
            throw new DatabaseException("Error invalidating reset tokens", e);
        }
    }
}
