package com.example.ecommerce.payment.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.payment.model.Payment;
import com.example.ecommerce.payment.model.PaymentTransactionStatus;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Payment transaction audit records in Microsoft SQL Server.
 */
public class PaymentDAO {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDAO.class);

    /**
     * Records a payment attempt or transaction within a connection.
     */
    public int createPayment(Payment payment, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.payments (order_id, payment_method, transaction_reference, " +
                     "amount, payment_status, gateway_response, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setString(2, payment.getPaymentMethod());
            stmt.setString(3, payment.getTransactionReference());
            stmt.setBigDecimal(4, payment.getAmount());
            stmt.setString(5, payment.getPaymentStatus().name());
            stmt.setString(6, payment.getGatewayResponse());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    payment.setPaymentId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to create payment record, no ID obtained.");
    }

    public int createPayment(Payment payment) {
        try (Connection conn = DBConnection.getConnection()) {
            return createPayment(payment, conn);
        } catch (SQLException e) {
            logger.error("Error creating payment for orderId: {}", payment.getOrderId(), e);
            throw new DatabaseException("Failed to record payment transaction", e);
        }
    }

    public Optional<Payment> findById(int paymentId) {
        String sql = "SELECT p.*, o.order_number, u.first_name, u.last_name, o.payment_status AS order_payment_status, o.order_status " +
                     "FROM dbo.payments p " +
                     "INNER JOIN dbo.orders o ON p.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                     "WHERE p.payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by ID: {}", paymentId, e);
            throw new DatabaseException("Error finding payment", e);
        }
        return Optional.empty();
    }

    public Optional<Payment> findByTransactionReference(String reference) {
        String sql = "SELECT p.*, o.order_number, u.first_name, u.last_name, o.payment_status AS order_payment_status, o.order_status " +
                     "FROM dbo.payments p " +
                     "INNER JOIN dbo.orders o ON p.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                     "WHERE p.transaction_reference = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reference);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by reference: {}", reference, e);
            throw new DatabaseException("Error retrieving payment by reference", e);
        }
        return Optional.empty();
    }

    public List<Payment> findByOrderId(int orderId) {
        String sql = "SELECT p.*, o.order_number, u.first_name, u.last_name, o.payment_status AS order_payment_status, o.order_status " +
                     "FROM dbo.payments p " +
                     "INNER JOIN dbo.orders o ON p.order_id = o.order_id " +
                     "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                     "WHERE p.order_id = ? ORDER BY p.created_at DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payments for order: {}", orderId, e);
            throw new DatabaseException("Error finding payments for order", e);
        }
        return list;
    }

    public void updatePaymentStatus(int paymentId, PaymentTransactionStatus status, String gatewayResponse, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.payments SET payment_status = ?, gateway_response = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE payment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, gatewayResponse);
            stmt.setInt(3, paymentId);
            stmt.executeUpdate();
        }
    }

    public void updatePaymentStatusByOrderId(int orderId, PaymentTransactionStatus status, String gatewayResponse, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.payments SET payment_status = ?, gateway_response = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, gatewayResponse);
            stmt.setInt(3, orderId);
            stmt.executeUpdate();
        }
    }

    public Pagination<Payment> findAll(String keyword, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (p.transaction_reference LIKE ? OR o.order_number LIKE ? OR u.email LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        String countSql = "SELECT COUNT(*) FROM dbo.payments p " +
                          "INNER JOIN dbo.orders o ON p.order_id = o.order_id " +
                          "INNER JOIN dbo.users u ON o.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object obj : params) countStmt.setObject(idx++, obj);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting payments", e);
            throw new DatabaseException("Error counting payments", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT p.*, o.order_number, u.first_name, u.last_name, o.payment_status AS order_payment_status, o.order_status " +
                         "FROM dbo.payments p " +
                         "INNER JOIN dbo.orders o ON p.order_id = o.order_id " +
                         "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                         where +
                         " ORDER BY p.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Payment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object obj : params) stmt.setObject(idx++, obj);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving payments", e);
            throw new DatabaseException("Error retrieving payment list", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public Map<String, Object> getPaymentSummaryStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) AS total_transactions, " +
                     "SUM(CASE WHEN (p.payment_status = 'SUCCESS' OR o.payment_status = 'PAID' OR o.order_status = 'DELIVERED') AND o.order_status NOT IN ('CANCELLED', 'RETURNED') AND p.payment_status != 'REFUNDED' THEN 1 ELSE 0 END) AS successful_transactions, " +
                     "SUM(CASE WHEN p.payment_status = 'FAILED' AND o.payment_status != 'PAID' THEN 1 ELSE 0 END) AS failed_transactions, " +
                     "SUM(CASE WHEN p.payment_status = 'REFUNDED' OR o.payment_status = 'REFUNDED' OR o.order_status = 'RETURNED' THEN 1 ELSE 0 END) AS refunded_transactions, " +
                     "COALESCE(SUM(CASE WHEN (p.payment_status = 'SUCCESS' OR o.payment_status = 'PAID' OR o.order_status = 'DELIVERED') AND o.order_status NOT IN ('CANCELLED', 'RETURNED') AND p.payment_status != 'REFUNDED' THEN p.amount ELSE 0 END), 0) AS total_collected " +
                     "FROM dbo.payments p " +
                     "INNER JOIN dbo.orders o ON p.order_id = o.order_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("totalTransactions", rs.getInt("total_transactions"));
                stats.put("successfulTransactions", rs.getInt("successful_transactions"));
                stats.put("failedTransactions", rs.getInt("failed_transactions"));
                stats.put("refundedTransactions", rs.getInt("refunded_transactions"));
                stats.put("totalCollected", rs.getBigDecimal("total_collected"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating payment statistics", e);
        }
        return stats;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionReference(rs.getString("transaction_reference"));
        p.setAmount(rs.getBigDecimal("amount"));

        String rawStatus = rs.getString("payment_status");
        PaymentTransactionStatus status = PaymentTransactionStatus.valueOf(rawStatus);

        // Reconcile status with order payment status if order was fulfilled/paid
        try {
            String orderPayStatus = rs.getString("order_payment_status");
            String orderStatus = rs.getString("order_status");
            if (status == PaymentTransactionStatus.PENDING && 
                ("PAID".equalsIgnoreCase(orderPayStatus) || "DELIVERED".equalsIgnoreCase(orderStatus))) {
                status = PaymentTransactionStatus.SUCCESS;
            }
        } catch (SQLException ignored) {
            // Column may not be in projection in custom queries
        }

        p.setPaymentStatus(status);
        p.setGatewayResponse(rs.getString("gateway_response"));
        p.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        p.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        p.setOrderNumber(rs.getString("order_number"));

        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        p.setCustomerName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
        return p;
    }
}
