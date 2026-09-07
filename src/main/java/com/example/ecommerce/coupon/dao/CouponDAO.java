package com.example.ecommerce.coupon.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.model.DiscountType;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Data Access Object for Coupons and Promotions in Microsoft SQL Server.
 */
@Repository
public class CouponDAO {

    private static final Logger logger = LoggerFactory.getLogger(CouponDAO.class);

    public Optional<Coupon> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM dbo.coupons WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCoupon(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding coupon by code: {}", code, e);
            throw new DatabaseException("Error finding coupon", e);
        }
        return Optional.empty();
    }

    public Optional<Coupon> findById(int couponId) {
        String sql = "SELECT * FROM dbo.coupons WHERE coupon_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCoupon(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding coupon by ID: {}", couponId, e);
            throw new DatabaseException("Error finding coupon by ID", e);
        }
        return Optional.empty();
    }

    public int createCoupon(Coupon coupon) {
        String sql = "INSERT INTO dbo.coupons (code, description, discount_type, discount_value, " +
                     "min_order_amount, max_discount_amount, start_date, end_date, usage_limit, current_usage, is_active, " +
                     "created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, coupon.getCode());
            stmt.setString(2, coupon.getDescription());
            stmt.setString(3, coupon.getDiscountType().name());
            stmt.setBigDecimal(4, coupon.getDiscountValue());
            stmt.setBigDecimal(5, coupon.getMinSpend());
            if (coupon.getMaxDiscount() != null) {
                stmt.setBigDecimal(6, coupon.getMaxDiscount());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            stmt.setTimestamp(7, coupon.getStartDate() != null ? Timestamp.valueOf(coupon.getStartDate()) : null);
            stmt.setTimestamp(8, coupon.getEndDate() != null ? Timestamp.valueOf(coupon.getEndDate()) : null);
            if (coupon.getUsageLimit() != null) {
                stmt.setInt(9, coupon.getUsageLimit());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setInt(10, coupon.getUsedCount());
            stmt.setBoolean(11, coupon.isActive());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    coupon.setCouponId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to create coupon, no generated ID obtained.");
        } catch (SQLException e) {
            logger.error("Error creating coupon: {}", coupon.getCode(), e);
            throw new DatabaseException("Failed to create coupon", e);
        }
    }

    public void updateCoupon(Coupon coupon) {
        String sql = "UPDATE dbo.coupons SET code = ?, description = ?, discount_type = ?, " +
                     "discount_value = ?, min_order_amount = ?, max_discount_amount = ?, start_date = ?, " +
                     "end_date = ?, usage_limit = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE coupon_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, coupon.getCode());
            stmt.setString(2, coupon.getDescription());
            stmt.setString(3, coupon.getDiscountType().name());
            stmt.setBigDecimal(4, coupon.getDiscountValue());
            stmt.setBigDecimal(5, coupon.getMinSpend());
            if (coupon.getMaxDiscount() != null) {
                stmt.setBigDecimal(6, coupon.getMaxDiscount());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            stmt.setTimestamp(7, coupon.getStartDate() != null ? Timestamp.valueOf(coupon.getStartDate()) : null);
            stmt.setTimestamp(8, coupon.getEndDate() != null ? Timestamp.valueOf(coupon.getEndDate()) : null);
            if (coupon.getUsageLimit() != null) {
                stmt.setInt(9, coupon.getUsageLimit());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setBoolean(10, coupon.isActive());
            stmt.setInt(11, coupon.getCouponId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating coupon ID: {}", coupon.getCouponId(), e);
            throw new DatabaseException("Failed to update coupon", e);
        }
    }

    public boolean incrementUsedCountAtomic(int couponId, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.coupons SET current_usage = current_usage + 1, "
                   + "updated_at = CURRENT_TIMESTAMP "
                   + "WHERE coupon_id = ? AND (usage_limit IS NULL OR current_usage < usage_limit)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0; // returns false if limit was already reached
        }
    }

    /**
     * Atomically decrements the usage count of a coupon when an order is cancelled.
     * Will not decrement below 0.
     */
    public void decrementUsedCount(int couponId, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.coupons SET current_usage = CASE WHEN current_usage > 0 THEN current_usage - 1 ELSE 0 END, "
                   + "updated_at = CURRENT_TIMESTAMP WHERE coupon_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            stmt.executeUpdate();
        }
    }

    public void setActiveStatus(int couponId, boolean active) {
        String sql = "UPDATE dbo.coupons SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE coupon_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, couponId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error setting active status on coupon ID: {}", couponId, e);
            throw new DatabaseException("Failed to update coupon status", e);
        }
    }

    public void deleteCoupon(int couponId) {
        String sql = "DELETE FROM dbo.coupons WHERE coupon_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting coupon ID: {}", couponId, e);
            throw new DatabaseException("Failed to delete coupon", e);
        }
    }

    public Pagination<Coupon> findAll(String keyword, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (code LIKE ? OR description LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }

        String countSql = "SELECT COUNT(*) FROM dbo.coupons " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object obj : params) countStmt.setObject(idx++, obj);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting coupons", e);
            throw new DatabaseException("Error counting coupons", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT * FROM dbo.coupons " + where + 
                         "ORDER BY created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Coupon> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object obj : params) stmt.setObject(idx++, obj);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCoupon(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving coupons", e);
            throw new DatabaseException("Error retrieving coupon list", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public Map<String, Object> getCouponSummaryStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) AS total_coupons, " +
                     "SUM(CASE WHEN CAST(is_active AS VARCHAR(5)) IN ('true', '1', 't') THEN 1 ELSE 0 END) AS active_coupons, " +
                     "COALESCE(SUM(current_usage), 0) AS total_redemptions " +
                     "FROM dbo.coupons";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("totalCoupons", rs.getInt("total_coupons"));
                stats.put("activeCoupons", rs.getInt("active_coupons"));
                stats.put("totalRedemptions", rs.getInt("total_redemptions"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating coupon statistics", e);
        }
        return stats;
    }

    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setCouponId(rs.getInt("coupon_id"));
        c.setCode(rs.getString("code"));
        try {
            c.setDescription(rs.getString("description"));
        } catch (SQLException ignored) {}
        c.setDiscountType(DiscountType.valueOf(rs.getString("discount_type")));
        c.setDiscountValue(rs.getBigDecimal("discount_value"));
        c.setMinSpend(rs.getBigDecimal("min_order_amount"));
        c.setMaxDiscount(rs.getBigDecimal("max_discount_amount"));
        c.setStartDate(rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null);
        c.setEndDate(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").toLocalDateTime() : null);
        int ul = rs.getInt("usage_limit");
        if (!rs.wasNull()) c.setUsageLimit(ul);
        c.setUsedCount(rs.getInt("current_usage"));
        c.setActive(rs.getBoolean("is_active"));
        c.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        c.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return c;
    }
}
