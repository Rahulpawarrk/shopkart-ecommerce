package com.example.ecommerce.admin.dao;

import com.example.ecommerce.admin.model.DashboardStats;
import com.example.ecommerce.admin.model.SalesReportItem;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for high-level administrative business intelligence and revenue metrics.
 */
public class DashboardDAO {

    private static final Logger logger = LoggerFactory.getLogger(DashboardDAO.class);

    public DashboardStats getDashboardKPIs() {
        DashboardStats stats = new DashboardStats();

        String sql = "SELECT " +
                     "(SELECT ISNULL(SUM(total_amount), 0) FROM dbo.orders WHERE (payment_status = 'PAID' OR order_status = 'DELIVERED') AND order_status != 'CANCELLED') AS total_revenue, " +
                     "(SELECT ISNULL(SUM(total_amount), 0) FROM dbo.orders WHERE (payment_status = 'PAID' OR order_status = 'DELIVERED') AND order_status != 'CANCELLED' AND CAST(created_at AS DATE) = CAST(SYSDATETIME() AS DATE)) AS today_revenue, " +
                     "(SELECT COUNT(*) FROM dbo.orders WHERE order_status != 'CANCELLED') AS total_orders, " +
                     "(SELECT COUNT(*) FROM dbo.orders WHERE order_status != 'CANCELLED' AND CAST(created_at AS DATE) = CAST(SYSDATETIME() AS DATE)) AS today_orders, " +
                     "(SELECT COUNT(*) FROM dbo.orders WHERE order_status IN ('PLACED', 'CONFIRMED', 'PROCESSING', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY')) AS pending_orders, " +
                     "(SELECT COUNT(*) FROM dbo.products WHERE status = 'ACTIVE') AS total_products, " +
                     "(SELECT COUNT(*) FROM dbo.inventory WHERE quantity <= low_stock_threshold) AS low_stock_products, " +
                     "(SELECT COUNT(DISTINCT ur.user_id) FROM dbo.user_roles ur INNER JOIN dbo.roles r ON ur.role_id = r.role_id WHERE r.role_name = 'CUSTOMER') AS total_customers";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.setTotalRevenue(rs.getBigDecimal("total_revenue"));
                stats.setTodayRevenue(rs.getBigDecimal("today_revenue"));
                stats.setTotalOrders(rs.getInt("total_orders"));
                stats.setTodayOrders(rs.getInt("today_orders"));
                stats.setPendingOrders(rs.getInt("pending_orders"));
                stats.setTotalProducts(rs.getInt("total_products"));
                stats.setLowStockProducts(rs.getInt("low_stock_products"));
                stats.setTotalCustomers(rs.getInt("total_customers"));
            }
        } catch (SQLException e) {
            logger.error("Error aggregating dashboard KPIs", e);
            throw new DatabaseException("Failed to load dashboard metrics", e);
        }

        return stats;
    }

    public List<SalesReportItem> getMonthlySalesReport(int year) {
        List<SalesReportItem> list = new ArrayList<>();
        String sql = "SELECT " +
                     "FORMAT(created_at, 'yyyy-MM') AS month_label, " +
                     "COUNT(order_id) AS order_count, " +
                     "ISNULL(SUM(total_amount), 0) AS total_sales " +
                     "FROM dbo.orders " +
                     "WHERE YEAR(created_at) = ? AND (payment_status = 'PAID' OR order_status = 'DELIVERED') AND order_status != 'CANCELLED' " +
                     "GROUP BY FORMAT(created_at, 'yyyy-MM') " +
                     "ORDER BY month_label ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SalesReportItem item = new SalesReportItem();
                    item.setLabel(rs.getString("month_label"));
                    item.setOrderCount(rs.getInt("order_count"));
                    item.setTotalSales(rs.getBigDecimal("total_sales"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Error aggregating monthly sales report for year: {}", year, e);
            throw new DatabaseException("Failed to generate monthly sales report", e);
        }
        return list;
    }

    public List<SalesReportItem> getCategoryRevenueReport() {
        List<SalesReportItem> list = new ArrayList<>();
        String sql = "SELECT " +
                     "c.category_name, " +
                     "COUNT(DISTINCT oi.order_id) AS order_count, " +
                     "SUM(oi.quantity) AS units_sold, " +
                     "SUM(oi.line_total) AS category_sales " +
                     "FROM dbo.order_items oi " +
                     "INNER JOIN dbo.products p ON oi.product_id = p.product_id " +
                     "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                     "INNER JOIN dbo.orders o ON oi.order_id = o.order_id " +
                     "WHERE (o.payment_status = 'PAID' OR o.order_status = 'DELIVERED') AND o.order_status != 'CANCELLED' " +
                     "GROUP BY c.category_name " +
                     "ORDER BY category_sales DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SalesReportItem item = new SalesReportItem();
                item.setLabel(rs.getString("category_name"));
                item.setOrderCount(rs.getInt("order_count"));
                item.setUnitsSold(rs.getInt("units_sold"));
                item.setTotalSales(rs.getBigDecimal("category_sales"));
                list.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error generating category revenue report", e);
            throw new DatabaseException("Failed to generate category report", e);
        }
        return list;
    }

    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP (?) " +
                     "p.product_id, p.product_name, p.sku, " +
                     "SUM(oi.quantity) AS total_units_sold, " +
                     "SUM(oi.line_total) AS total_revenue " +
                     "FROM dbo.order_items oi " +
                     "INNER JOIN dbo.products p ON oi.product_id = p.product_id " +
                     "INNER JOIN dbo.orders o ON oi.order_id = o.order_id " +
                     "WHERE (o.payment_status = 'PAID' OR o.order_status = 'DELIVERED') AND o.order_status != 'CANCELLED' " +
                     "GROUP BY p.product_id, p.product_name, p.sku " +
                     "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", rs.getInt("product_id"));
                    map.put("productName", rs.getString("product_name"));
                    map.put("sku", rs.getString("sku"));
                    map.put("unitsSold", rs.getInt("total_units_sold"));
                    map.put("totalRevenue", rs.getBigDecimal("total_revenue"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating top selling products", e);
            throw new DatabaseException("Failed to retrieve top products", e);
        }
        return list;
    }
}
