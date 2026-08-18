package com.example.ecommerce.payment.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.payment.model.PaymentReconciliation;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Data Access Object for Payment Failures and Dispute Reconciliation records.
 */
public class PaymentReconciliationDAO {

    private static final Logger logger = LoggerFactory.getLogger(PaymentReconciliationDAO.class);

    /**
     * Records a failed payment transaction for audit and customer support reconciliation.
     */
    public int createRecord(PaymentReconciliation recon, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.payment_reconciliation " +
                     "(order_id, user_id, transaction_reference, gateway_order_id, payment_method, " +
                     " amount, failure_reason, gateway_response, reconciliation_status, admin_notes, " +
                     " created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, recon.getOrderId());
            stmt.setInt(2, recon.getUserId());
            stmt.setString(3, recon.getTransactionReference());
            stmt.setString(4, recon.getGatewayOrderId());
            stmt.setString(5, recon.getPaymentMethod());
            stmt.setBigDecimal(6, recon.getAmount() != null ? recon.getAmount() : BigDecimal.ZERO);
            stmt.setString(7, recon.getFailureReason());
            stmt.setString(8, recon.getGatewayResponse());
            stmt.setString(9, recon.getReconciliationStatus() != null ? recon.getReconciliationStatus() : "PENDING");
            stmt.setString(10, recon.getAdminNotes());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    recon.setReconciliationId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to create payment reconciliation record, no ID obtained.");
    }

    public int createRecord(PaymentReconciliation recon) {
        try (Connection conn = DBConnection.getConnection()) {
            return createRecord(recon, conn);
        } catch (SQLException e) {
            logger.error("Error creating payment reconciliation entry for orderId: {}", recon.getOrderId(), e);
            throw new DatabaseException("Failed to record payment reconciliation entry", e);
        }
    }

    public Optional<PaymentReconciliation> findById(int reconciliationId) {
        String sql = "SELECT pr.*, o.order_number, " +
                     "u.first_name AS cust_fn, u.last_name AS cust_ln, u.email AS cust_email, u.phone AS cust_phone, " +
                     "admin.first_name AS admin_fn, admin.last_name AS admin_ln " +
                     "FROM dbo.payment_reconciliation pr " +
                     "INNER JOIN dbo.orders o ON pr.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON pr.user_id = u.user_id " +
                     "LEFT JOIN dbo.users admin ON pr.resolved_by = admin.user_id " +
                     "WHERE pr.reconciliation_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reconciliationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReconciliation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reconciliation record by ID: {}", reconciliationId, e);
            throw new DatabaseException("Error finding reconciliation record", e);
        }
        return Optional.empty();
    }

    public List<PaymentReconciliation> findByOrderId(int orderId) {
        String sql = "SELECT pr.*, o.order_number, " +
                     "u.first_name AS cust_fn, u.last_name AS cust_ln, u.email AS cust_email, u.phone AS cust_phone, " +
                     "admin.first_name AS admin_fn, admin.last_name AS admin_ln " +
                     "FROM dbo.payment_reconciliation pr " +
                     "INNER JOIN dbo.orders o ON pr.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON pr.user_id = u.user_id " +
                     "LEFT JOIN dbo.users admin ON pr.resolved_by = admin.user_id " +
                     "WHERE pr.order_id = ? " +
                     "ORDER BY pr.created_at DESC";

        List<PaymentReconciliation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReconciliation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reconciliation records for order: {}", orderId, e);
            throw new DatabaseException("Error finding reconciliation records for order", e);
        }
        return list;
    }

    public Pagination<PaymentReconciliation> findAll(String keyword, String status, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            where.append("AND pr.reconciliation_status = ? ");
            params.add(status.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (pr.transaction_reference LIKE ? OR o.order_number LIKE ? OR u.email LIKE ? OR u.phone LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        String countSql = "SELECT COUNT(*) FROM dbo.payment_reconciliation pr " +
                          "INNER JOIN dbo.orders o ON pr.order_id = o.order_id " +
                          "INNER JOIN dbo.users u ON pr.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object obj : params) countStmt.setObject(idx++, obj);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting reconciliation records", e);
            throw new DatabaseException("Error counting reconciliation records", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT pr.*, o.order_number, " +
                         "u.first_name AS cust_fn, u.last_name AS cust_ln, u.email AS cust_email, u.phone AS cust_phone, " +
                         "admin.first_name AS admin_fn, admin.last_name AS admin_ln " +
                         "FROM dbo.payment_reconciliation pr " +
                         "INNER JOIN dbo.orders o ON pr.order_id = o.order_id " +
                         "INNER JOIN dbo.users u ON pr.user_id = u.user_id " +
                         "LEFT JOIN dbo.users admin ON pr.resolved_by = admin.user_id " +
                         where +
                         " ORDER BY pr.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<PaymentReconciliation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object obj : params) stmt.setObject(idx++, obj);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReconciliation(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving reconciliation records", e);
            throw new DatabaseException("Error retrieving reconciliation records", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public boolean updateResolution(int reconciliationId, String newStatus, String adminNotes, int adminUserId) {
        String sql = "UPDATE dbo.payment_reconciliation " +
                     "SET reconciliation_status = ?, admin_notes = ?, resolved_by = ?, " +
                     "    resolved_at = CASE WHEN ? IN ('RESOLVED', 'REFUND_COMPLETED', 'MANUALLY_CREDITED', 'NOT_DEBITED') THEN SYSDATETIME() ELSE resolved_at END, " +
                     "    updated_at = SYSDATETIME() " +
                     "WHERE reconciliation_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, adminNotes);
            stmt.setInt(3, adminUserId);
            stmt.setString(4, newStatus);
            stmt.setInt(5, reconciliationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating reconciliation status for ID: {}", reconciliationId, e);
            throw new DatabaseException("Failed to update reconciliation status", e);
        }
    }

    public Map<String, Object> getReconciliationStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) AS total_failed, " +
                     "SUM(CASE WHEN reconciliation_status = 'PENDING' THEN 1 ELSE 0 END) AS pending_audit, " +
                     "SUM(CASE WHEN reconciliation_status = 'VERIFIED_DEBITED' THEN 1 ELSE 0 END) AS verified_debited, " +
                     "SUM(CASE WHEN reconciliation_status = 'REFUND_INITIATED' THEN 1 ELSE 0 END) AS refund_initiated, " +
                     "SUM(CASE WHEN reconciliation_status IN ('RESOLVED', 'REFUND_COMPLETED', 'MANUALLY_CREDITED', 'NOT_DEBITED') THEN 1 ELSE 0 END) AS resolved_count, " +
                     "ISNULL(SUM(amount), 0) AS total_disputed_amount " +
                     "FROM dbo.payment_reconciliation";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("totalFailed", rs.getInt("total_failed"));
                stats.put("pendingAudit", rs.getInt("pending_audit"));
                stats.put("verifiedDebited", rs.getInt("verified_debited"));
                stats.put("refundInitiated", rs.getInt("refund_initiated"));
                stats.put("resolvedCount", rs.getInt("resolved_count"));
                stats.put("totalDisputedAmount", rs.getBigDecimal("total_disputed_amount"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating reconciliation statistics", e);
        }
        return stats;
    }

    private PaymentReconciliation mapResultSetToReconciliation(ResultSet rs) throws SQLException {
        PaymentReconciliation pr = new PaymentReconciliation();
        pr.setReconciliationId(rs.getInt("reconciliation_id"));
        pr.setOrderId(rs.getInt("order_id"));
        pr.setUserId(rs.getInt("user_id"));
        pr.setTransactionReference(rs.getString("transaction_reference"));
        pr.setGatewayOrderId(rs.getString("gateway_order_id"));
        pr.setPaymentMethod(rs.getString("payment_method"));
        pr.setAmount(rs.getBigDecimal("amount"));
        pr.setFailureReason(rs.getString("failure_reason"));
        pr.setGatewayResponse(rs.getString("gateway_response"));
        pr.setReconciliationStatus(rs.getString("reconciliation_status"));
        pr.setAdminNotes(rs.getString("admin_notes"));

        int resolvedByVal = rs.getInt("resolved_by");
        if (!rs.wasNull()) {
            pr.setResolvedBy(resolvedByVal);
        }

        pr.setResolvedAt(rs.getTimestamp("resolved_at") != null ? rs.getTimestamp("resolved_at").toLocalDateTime() : null);
        pr.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        pr.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);

        pr.setOrderNumber(rs.getString("order_number"));

        String custFn = rs.getString("cust_fn");
        String custLn = rs.getString("cust_ln");
        pr.setCustomerName(((custFn != null ? custFn : "") + " " + (custLn != null ? custLn : "")).trim());
        pr.setCustomerEmail(rs.getString("cust_email"));
        pr.setCustomerPhone(rs.getString("cust_phone"));

        String admFn = rs.getString("admin_fn");
        String admLn = rs.getString("admin_ln");
        if (admFn != null || admLn != null) {
            pr.setResolverName(((admFn != null ? admFn : "") + " " + (admLn != null ? admLn : "")).trim());
        }

        return pr;
    }
}
