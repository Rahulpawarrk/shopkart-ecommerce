package com.example.ecommerce.order.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.OrderStatusHistory;
import com.example.ecommerce.order.model.PaymentStatus;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object for Orders, Order Line Items, and Lifecycle Status
 * Histories.
 */
public class OrderDAO {

    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);

    /**
     * Inserts master order record within the active checkout transaction.
     */
    public int createOrder(Order order, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.orders (order_number, user_id, order_status, payment_status, " +
                "payment_method, subtotal, discount_amount, tax_amount, shipping_amount, total_amount, " +
                "coupon_id, shipping_full_name, shipping_phone, shipping_address_line1, shipping_address_line2, " +
                "shipping_city, shipping_state, shipping_postal_code, shipping_country, billing_address_snapshot, " +
                "notes, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getOrderNumber());
            stmt.setInt(2, order.getUserId());
            stmt.setString(3, order.getOrderStatus().name());
            stmt.setString(4, order.getPaymentStatus().name());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setBigDecimal(6, order.getSubtotal());
            stmt.setBigDecimal(7, order.getDiscountAmount());
            stmt.setBigDecimal(8, order.getTaxAmount());
            stmt.setBigDecimal(9, order.getShippingAmount());
            stmt.setBigDecimal(10, order.getTotalAmount());
            if (order.getCouponId() != null) {
                stmt.setInt(11, order.getCouponId());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            stmt.setString(12, order.getShippingFullName());
            stmt.setString(13, order.getShippingPhone());
            stmt.setString(14, order.getShippingAddressLine1());
            stmt.setString(15, order.getShippingAddressLine2());
            stmt.setString(16, order.getShippingCity());
            stmt.setString(17, order.getShippingState());
            stmt.setString(18, order.getShippingPostalCode());
            stmt.setString(19, order.getShippingCountry());
            stmt.setString(20, order.getBillingAddressSnapshot());
            stmt.setString(21, order.getNotes());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int orderId = keys.getInt(1);
                    order.setOrderId(orderId);
                    return orderId;
                }
            }
        }
        throw new SQLException("Failed to create order, no generated key obtained.");
    }

    /**
     * Inserts immutable order line item snapshot within transaction.
     */
    public void createOrderItem(OrderItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.order_items (order_id, product_id, product_name, sku, unit_price, " +
                "discount_amount, tax_amount, line_total, quantity, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setString(4, item.getSku());
            stmt.setBigDecimal(5, item.getUnitPrice());
            stmt.setBigDecimal(6, item.getDiscountAmount());
            stmt.setBigDecimal(7, item.getTaxAmount());
            stmt.setBigDecimal(8, item.getLineTotal());
            stmt.setInt(9, item.getQuantity());
            stmt.executeUpdate();
        }
    }

    /**
     * Appends an audit status history record within transaction.
     */
    public void createStatusHistory(OrderStatusHistory history, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.order_status_history (order_id, previous_status, new_status, " +
                "changed_by, remarks, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, history.getOrderId());
            stmt.setString(2, history.getPreviousStatus() != null ? history.getPreviousStatus().name() : null);
            stmt.setString(3, history.getNewStatus().name());
            if (history.getChangedBy() != null) {
                stmt.setInt(4, history.getChangedBy());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, history.getRemarks());
            stmt.executeUpdate();
        }
    }

    /**
     * Finds order by ID with customer metadata.
     */
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email " +
                "FROM dbo.orders o " +
                "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                "WHERE o.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(orderId, conn));
                    order.setStatusHistory(getStatusHistory(orderId, conn));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving order by ID: {}", orderId, e);
            throw new DatabaseException("Error retrieving order details", e);
        }
        return Optional.empty();
    }

    /**
     * Finds order by human-readable Order Number.
     */
    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email " +
                "FROM dbo.orders o " +
                "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                "WHERE o.order_number = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    order.setStatusHistory(getStatusHistory(order.getOrderId(), conn));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by number: {}", orderNumber, e);
            throw new DatabaseException("Error finding order by number", e);
        }
        return Optional.empty();
    }

    /**
     * Finds order by courier tracking number / AWB.
     */
    public Optional<Order> findByTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT o.*, u.first_name, u.last_name, u.email " +
                "FROM dbo.orders o " +
                "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                "WHERE o.tracking_number = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trackingNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    order.setStatusHistory(getStatusHistory(order.getOrderId(), conn));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding order by tracking number: {}", trackingNumber, e);
            throw new DatabaseException("Error finding order by tracking number", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves paginated orders for a specific customer.
     */
    public Pagination<Order> findByUserId(int userId, int page, int pageSize) {
        return findByUserId(userId, null, null, page, pageSize);
    }

    public Pagination<Order> findByUserId(int userId, String statusFilter, String sortBy, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE o.user_id = ? AND (o.payment_method = 'COD' OR o.payment_status IN ('PAID', 'COMPLETED') OR o.order_status NOT IN ('PENDING', 'CANCELLED')) ");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            String sf = statusFilter.trim().toUpperCase();
            if ("PROCESSING".equals(sf) || "IN_PROGRESS".equals(sf)) {
                where.append("AND o.order_status IN ('PENDING', 'CONFIRMED', 'PROCESSING') ");
            } else if ("DISPATCHED".equals(sf)) {
                where.append("AND o.order_status = 'DISPATCHED' ");
            } else if ("SHIPPED".equals(sf) || "IN_TRANSIT".equals(sf)) {
                where.append("AND o.order_status IN ('SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY') ");
            } else if ("DELIVERED".equals(sf)) {
                where.append("AND o.order_status = 'DELIVERED' ");
            } else if ("CANCELLED".equals(sf)) {
                where.append("AND o.order_status IN ('CANCELLED', 'RETURNED', 'RETURN_REQUESTED') ");
            } else {
                where.append("AND o.order_status = ? ");
                params.add(sf);
            }
        }

        String countSql = "SELECT COUNT(*) FROM dbo.orders o " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            for (int i = 0; i < params.size(); i++) {
                countStmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next())
                    total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting user orders", e);
            throw new DatabaseException("Error counting customer orders", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String orderByClause = " ORDER BY o.created_at DESC ";
        if ("oldest".equalsIgnoreCase(sortBy)) {
            orderByClause = " ORDER BY o.created_at ASC ";
        } else if ("price_high".equalsIgnoreCase(sortBy)) {
            orderByClause = " ORDER BY o.total_amount DESC ";
        } else if ("price_low".equalsIgnoreCase(sortBy)) {
            orderByClause = " ORDER BY o.total_amount ASC ";
        } else if ("order_num".equalsIgnoreCase(sortBy)) {
            orderByClause = " ORDER BY o.order_number DESC ";
        }

        String dataSql = "SELECT o.*, u.first_name, u.last_name, u.email " +
                "FROM dbo.orders o " +
                "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                where +
                orderByClause +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Order> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int paramIndex = 1;
            for (Object param : params) {
                stmt.setObject(paramIndex++, param);
            }
            stmt.setInt(paramIndex++, (page - 1) * pageSize);
            stmt.setInt(paramIndex, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user orders", e);
            throw new DatabaseException("Error retrieving order history", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    /**
     * Admin: Searches and paginates all store orders.
     */
    public Pagination<Order> findAll(String keyword, OrderStatus status, int page, int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append("AND (o.order_number LIKE ? OR u.email LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null) {
            where.append("AND o.order_status = ? ");
            params.add(status.name());
        }

        String countSql = "SELECT COUNT(*) FROM dbo.orders o INNER JOIN dbo.users u ON o.user_id = u.user_id " + where;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            int idx = 1;
            for (Object p : params)
                countStmt.setObject(idx++, p);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next())
                    total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting admin orders", e);
            throw new DatabaseException("Error counting orders", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT o.*, u.first_name, u.last_name, u.email " +
                "FROM dbo.orders o " +
                "INNER JOIN dbo.users u ON o.user_id = u.user_id " +
                where +
                " ORDER BY o.created_at DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Order> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = 1;
            for (Object p : params)
                stmt.setObject(idx++, p);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching admin orders", e);
            throw new DatabaseException("Error fetching admin orders", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    public List<OrderItem> getOrderItems(int orderId, Connection conn) throws SQLException {
        String sql = "SELECT order_item_id, order_id, product_id, product_name, sku, unit_price, " +
                "discount_amount, tax_amount, line_total, quantity, created_at " +
                "FROM dbo.order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setSku(rs.getString("sku"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setDiscountAmount(rs.getBigDecimal("discount_amount"));
                    item.setTaxAmount(rs.getBigDecimal("tax_amount"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null);
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<OrderStatusHistory> getStatusHistory(int orderId, Connection conn) throws SQLException {
        String sql = "SELECT osh.history_id, osh.order_id, osh.previous_status, osh.new_status, " +
                "osh.changed_by, osh.remarks, osh.created_at, u.first_name, u.last_name " +
                "FROM dbo.order_status_history osh " +
                "LEFT JOIN dbo.users u ON osh.changed_by = u.user_id " +
                "WHERE osh.order_id = ? ORDER BY osh.created_at ASC";
        List<OrderStatusHistory> histories = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderStatusHistory h = new OrderStatusHistory();
                    h.setHistoryId(rs.getInt("history_id"));
                    h.setOrderId(rs.getInt("order_id"));
                    String prev = rs.getString("previous_status");
                    if (prev != null)
                        h.setPreviousStatus(OrderStatus.valueOf(prev));
                    h.setNewStatus(OrderStatus.valueOf(rs.getString("new_status")));
                    int cb = rs.getInt("changed_by");
                    if (!rs.wasNull())
                        h.setChangedBy(cb);
                    h.setRemarks(rs.getString("remarks"));
                    h.setCreatedAt(
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null);

                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    if (fn != null || ln != null) {
                        h.setChangedByName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
                    } else {
                        h.setChangedByName("Customer / System");
                    }
                    histories.add(h);
                }
            }
        }
        return histories;
    }

    public void updateOrderStatus(int orderId, OrderStatus newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.orders SET order_status = ?, " +
                "delivered_at = CASE WHEN ? = 'DELIVERED' THEN CURRENT_TIMESTAMP ELSE delivered_at END, " +
                "updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setString(2, newStatus.name());
            stmt.setInt(3, orderId);
            stmt.executeUpdate();
        }
    }

    public void updateOrderFulfillment(int orderId, OrderStatus newStatus, String courierPartner,
            String trackingNumber, String deliveryAgentPhone, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.orders SET order_status = ?, " +
                "courier_partner = COALESCE(?, courier_partner), " +
                "tracking_number = COALESCE(?, tracking_number), " +
                "delivery_agent_phone = COALESCE(?, delivery_agent_phone), " +
                "delivered_at = CASE WHEN ? = 'DELIVERED' THEN CURRENT_TIMESTAMP ELSE delivered_at END, " +
                "updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setString(2,
                    courierPartner != null && !courierPartner.trim().isEmpty() ? courierPartner.trim() : null);
            stmt.setString(3,
                    trackingNumber != null && !trackingNumber.trim().isEmpty() ? trackingNumber.trim() : null);
            stmt.setString(4,
                    deliveryAgentPhone != null && !deliveryAgentPhone.trim().isEmpty() ? deliveryAgentPhone.trim()
                            : null);
            stmt.setString(5, newStatus.name());
            stmt.setInt(6, orderId);
            stmt.executeUpdate();
        }
    }

    public void updatePaymentStatus(int orderId, PaymentStatus newStatus, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.orders SET payment_status = ?, updated_at = CURRENT_TIMESTAMP WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }

        if (newStatus == PaymentStatus.PAID) {
            String syncPaySql = "UPDATE dbo.payments SET payment_status = 'SUCCESS', updated_at = CURRENT_TIMESTAMP WHERE order_id = ? AND payment_status = 'PENDING'";
            try (PreparedStatement payStmt = conn.prepareStatement(syncPaySql)) {
                payStmt.setInt(1, orderId);
                int updatedCount = payStmt.executeUpdate();
                if (updatedCount == 0) {
                    String checkSql = "SELECT COUNT(*) FROM dbo.payments WHERE order_id = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, orderId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                String insertPay = "INSERT INTO dbo.payments (order_id, payment_method, transaction_reference, amount, payment_status, gateway_response, created_at, updated_at) "
                                        +
                                        "SELECT order_id, payment_method, CONCAT('PAY-', UPPER(payment_method), '-ORD', CAST(order_id AS VARCHAR)), total_amount, 'SUCCESS', 'Settled order payment', created_at, CURRENT_TIMESTAMP "
                                        +
                                        "FROM dbo.orders WHERE order_id = ?";
                                try (PreparedStatement insStmt = conn.prepareStatement(insertPay)) {
                                    insStmt.setInt(1, orderId);
                                    insStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculates comprehensive order lifecycle metrics for admin reporting.
     */
    public Map<String, Object> getOrderSummaryStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                "COUNT(order_id) AS total_orders, " +
                "COALESCE(SUM(total_amount), 0) AS gross_sales, " +
                "SUM(CASE WHEN order_status = 'DELIVERED' THEN 1 ELSE 0 END) AS delivered_count, " +
                "SUM(CASE WHEN order_status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count, " +
                "SUM(CASE WHEN order_status IN ('PLACED', 'CONFIRMED', 'PROCESSING', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY') THEN 1 ELSE 0 END) AS pending_count, "
                +
                "SUM(CASE WHEN payment_status = 'PAID' THEN 1 ELSE 0 END) AS paid_count, " +
                "SUM(CASE WHEN payment_status = 'PENDING' THEN 1 ELSE 0 END) AS pending_payment_count, " +
                "COALESCE(SUM(CASE WHEN order_status = 'DELIVERED' THEN total_amount ELSE 0 END), 0) AS delivered_revenue "
                +
                "FROM dbo.orders";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("totalOrders", rs.getInt("total_orders"));
                stats.put("grossSales", rs.getBigDecimal("gross_sales"));
                stats.put("deliveredCount", rs.getInt("delivered_count"));
                stats.put("cancelledCount", rs.getInt("cancelled_count"));
                stats.put("pendingCount", rs.getInt("pending_count"));
                stats.put("paidCount", rs.getInt("paid_count"));
                stats.put("pendingPaymentCount", rs.getInt("pending_payment_count"));
                stats.put("deliveredRevenue", rs.getBigDecimal("delivered_revenue"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating order statistics", e);
            throw new DatabaseException("Failed to calculate order statistics", e);
        }
        return stats;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setUserId(rs.getInt("user_id"));
        o.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        o.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setTaxAmount(rs.getBigDecimal("tax_amount"));
        o.setShippingAmount(rs.getBigDecimal("shipping_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        int cid = rs.getInt("coupon_id");
        if (!rs.wasNull())
            o.setCouponId(cid);

        o.setShippingFullName(rs.getString("shipping_full_name"));
        o.setShippingPhone(rs.getString("shipping_phone"));
        o.setShippingAddressLine1(rs.getString("shipping_address_line1"));
        o.setShippingAddressLine2(rs.getString("shipping_address_line2"));
        o.setShippingCity(rs.getString("shipping_city"));
        o.setShippingState(rs.getString("shipping_state"));
        o.setShippingPostalCode(rs.getString("shipping_postal_code"));
        o.setShippingCountry(rs.getString("shipping_country"));
        o.setBillingAddressSnapshot(rs.getString("billing_address_snapshot"));
        o.setNotes(rs.getString("notes"));

        try {
            o.setCourierPartner(rs.getString("courier_partner"));
            o.setTrackingNumber(rs.getString("tracking_number"));
            o.setDeliveryAgentPhone(rs.getString("delivery_agent_phone"));
            if (rs.getTimestamp("estimated_delivery_date") != null) {
                o.setEstimatedDeliveryDate(rs.getTimestamp("estimated_delivery_date").toLocalDateTime());
            }
            if (rs.getTimestamp("delivered_at") != null) {
                o.setDeliveredAt(rs.getTimestamp("delivered_at").toLocalDateTime());
            }
        } catch (SQLException ignored) {
            // Field may be missing in legacy query contexts
        }

        o.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        o.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);

        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        o.setCustomerName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
        o.setCustomerEmail(rs.getString("email"));
        return o;
    }
}
