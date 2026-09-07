package com.example.ecommerce.order.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Data Access Object for Order Returns and Replacements in Microsoft SQL Server.
 */
@Repository
public class OrderReturnDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderReturnDAO.class);

    public int createReturn(OrderReturn ret) {
        String sql = "INSERT INTO dbo.order_returns (order_id, user_id, return_number, return_reason, " +
                     "resolution_type, comments, image_url, return_status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ret.getOrderId());
            stmt.setInt(2, ret.getUserId());
            stmt.setString(3, ret.getReturnNumber());
            stmt.setString(4, ret.getReturnReason());
            stmt.setString(5, ret.getResolutionType());
            stmt.setString(6, ret.getComments());
            stmt.setString(7, ret.getImageUrl());
            stmt.setString(8, ret.getReturnStatus() != null ? ret.getReturnStatus() : "REQUESTED");

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ret.setReturnId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to create order return, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error creating order return for orderId: {}", ret.getOrderId(), e);
            throw new DatabaseException("Failed to submit return request", e);
        }
    }

    public Optional<OrderReturn> findByOrderId(int orderId) {
        String sql = "SELECT r.*, o.order_number, u.first_name, u.last_name, u.email " +
                     "FROM dbo.order_returns r " +
                     "INNER JOIN dbo.orders o ON r.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                     "WHERE r.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrderReturn(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding return for orderId: {}", orderId, e);
            throw new DatabaseException("Error finding return request", e);
        }
        return Optional.empty();
    }

    public Optional<OrderReturn> findById(int returnId) {
        String sql = "SELECT r.*, o.order_number, u.first_name, u.last_name, u.email " +
                     "FROM dbo.order_returns r " +
                     "INNER JOIN dbo.orders o ON r.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                     "WHERE r.return_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, returnId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrderReturn(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding return by ID: {}", returnId, e);
            throw new DatabaseException("Error retrieving return record", e);
        }
        return Optional.empty();
    }

    public Pagination<OrderReturn> findByUserId(int userId, int page, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM dbo.order_returns WHERE user_id = ?";
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, userId);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting user returns for userId: {}", userId, e);
            throw new DatabaseException("Error counting returns", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT r.*, o.order_number, u.first_name, u.last_name, u.email " +
                         "FROM dbo.order_returns r " +
                         "INNER JOIN dbo.orders o ON r.order_id = o.order_id " +
                         "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                         "WHERE r.user_id = ? " +
                         "ORDER BY r.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<OrderReturn> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, (page - 1) * pageSize);
            stmt.setInt(3, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrderReturn(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching user returns for userId: {}", userId, e);
            throw new DatabaseException("Error fetching user returns", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public Pagination<OrderReturn> findAll(String keyword, String status, int page, int pageSize) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (r.return_number LIKE ? OR o.order_number LIKE ? OR u.email LIKE ? OR u.first_name LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            where.append("AND r.return_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        String countSql = "SELECT COUNT(*) FROM dbo.order_returns r " +
                          "INNER JOIN dbo.orders o ON r.order_id = o.order_id " +
                          "INNER JOIN dbo.users u ON r.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object obj : params) countStmt.setObject(idx++, obj);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting admin returns", e);
            throw new DatabaseException("Error counting returns", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT r.*, o.order_number, u.first_name, u.last_name, u.email " +
                         "FROM dbo.order_returns r " +
                         "INNER JOIN dbo.orders o ON r.order_id = o.order_id " +
                         "INNER JOIN dbo.users u ON r.user_id = u.user_id " +
                         where +
                         " ORDER BY r.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<OrderReturn> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object obj : params) stmt.setObject(idx++, obj);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrderReturn(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving admin returns", e);
            throw new DatabaseException("Error retrieving returns", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public boolean updateStatus(int returnId, String newStatus, String adminNotes, BigDecimal refundAmount) {
        String sql = "UPDATE dbo.order_returns SET return_status = ?, " +
                     "admin_notes = COALESCE(?, admin_notes), " +
                     "refund_amount = COALESCE(?, refund_amount), " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "WHERE return_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, adminNotes);
            stmt.setObject(3, refundAmount);
            stmt.setInt(4, returnId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating return status for returnId: {}", returnId, e);
            throw new DatabaseException("Failed to update return status", e);
        }
    }

    private OrderReturn mapResultSetToOrderReturn(ResultSet rs) throws SQLException {
        OrderReturn r = new OrderReturn();
        r.setReturnId(rs.getInt("return_id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setReturnNumber(rs.getString("return_number"));
        r.setReturnReason(rs.getString("return_reason"));
        r.setResolutionType(rs.getString("resolution_type"));
        r.setComments(rs.getString("comments"));
        r.setImageUrl(rs.getString("image_url"));
        r.setReturnStatus(rs.getString("return_status"));
        r.setRefundAmount(rs.getBigDecimal("refund_amount"));
        r.setAdminNotes(rs.getString("admin_notes"));

        Timestamp pickup = rs.getTimestamp("pickup_date");
        if (pickup != null) r.setPickupDate(pickup.toLocalDateTime());

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) r.setUpdatedAt(updated.toLocalDateTime());

        r.setOrderNumber(rs.getString("order_number"));
        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        r.setCustomerName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
        r.setCustomerEmail(rs.getString("email"));
        return r;
    }
}
