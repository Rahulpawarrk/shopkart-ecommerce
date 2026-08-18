package com.example.ecommerce.inventory.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.inventory.model.InventoryTransaction;
import com.example.ecommerce.inventory.model.TransactionType;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data Access Object managing real-time inventory balances, row-level locking,
 * and immutable stock transaction audit ledgers.
 */
public class InventoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(InventoryDAO.class);

    /**
     * Searches and paginates inventory records with optional low/out-of-stock filters.
     */
    public Pagination<Inventory> findAll(String keyword, String filter, int page, int pageSize) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildFilterConditions(keyword, filter, whereClause, params);

        // 1. Total Count
        String countSql = "SELECT COUNT(*) FROM dbo.inventory i " +
                          "INNER JOIN dbo.products p ON i.product_id = p.product_id " +
                          "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                          whereClause;
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            setStatementParameters(countStmt, params);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting inventory records", e);
            throw new DatabaseException("Error counting inventory records", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        // 2. Fetch Page Data with SQL Server OFFSET-FETCH
        String dataSql = "SELECT i.inventory_id, i.product_id, i.quantity, i.low_stock_threshold, i.last_updated, " +
                         "p.product_name, p.sku, p.brand, p.price, c.category_name " +
                         "FROM dbo.inventory i " +
                         "INNER JOIN dbo.products p ON i.product_id = p.product_id " +
                         "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                         whereClause +
                         " ORDER BY i.quantity ASC, p.product_name ASC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Inventory> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            int idx = setStatementParameters(stmt, params);
            stmt.setInt(idx++, (page - 1) * pageSize);
            stmt.setInt(idx, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInventory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving inventory records", e);
            throw new DatabaseException("Error retrieving inventory list", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    /**
     * Retrieves inventory details by product ID.
     */
    public Optional<Inventory> findByProductId(int productId) {
        String sql = "SELECT i.inventory_id, i.product_id, i.quantity, i.low_stock_threshold, i.last_updated, " +
                     "p.product_name, p.sku, p.brand, p.price, c.category_name " +
                     "FROM dbo.inventory i " +
                     "INNER JOIN dbo.products p ON i.product_id = p.product_id " +
                     "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                     "WHERE i.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInventory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding inventory for productId: {}", productId, e);
            throw new DatabaseException("Error retrieving inventory", e);
        }
        return Optional.empty();
    }

    /**
     * Obtains an exclusive row lock on product inventory (WITH (UPDLOCK, ROWLOCK))
     * within an active transaction to prevent concurrent race conditions.
     */
    public Optional<Inventory> getInventoryWithLock(int productId, Connection conn) throws SQLException {
        String sql = "SELECT i.inventory_id, i.product_id, i.quantity, i.low_stock_threshold, i.last_updated, " +
                     "p.product_name, p.sku, p.brand, p.price, c.category_name " +
                     "FROM dbo.inventory i WITH (UPDLOCK, ROWLOCK) " +
                     "INNER JOIN dbo.products p ON i.product_id = p.product_id " +
                     "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                     "WHERE i.product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToInventory(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Updates real-time stock quantity within transaction.
     */
    public boolean updateStock(int productId, int newQuantity, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.inventory SET quantity = ?, last_updated = SYSDATETIME() WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the low stock alert threshold for a product.
     */
    public boolean updateThreshold(int productId, int lowStockThreshold) {
        String sql = "UPDATE dbo.inventory SET low_stock_threshold = ?, last_updated = SYSDATETIME() WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lowStockThreshold);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating threshold for productId: {}", productId, e);
            throw new DatabaseException("Error updating low stock threshold", e);
        }
    }

    /**
     * Appends an immutable stock audit transaction record in the transaction.
     */
    public void recordTransaction(InventoryTransaction t, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.inventory_transactions (product_id, previous_stock, quantity_changed, " +
                     "new_stock, transaction_type, reference_type, reference_id, remarks, created_by, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, t.getProductId());
            stmt.setInt(2, t.getPreviousStock());
            stmt.setInt(3, t.getQuantityChanged());
            stmt.setInt(4, t.getNewStock());
            stmt.setString(5, t.getTransactionType().name());
            stmt.setString(6, t.getReferenceType());
            if (t.getReferenceId() != null) {
                stmt.setInt(7, t.getReferenceId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, t.getRemarks());
            if (t.getCreatedBy() != null) {
                stmt.setInt(9, t.getCreatedBy());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves paginated transaction history / audit log for a product.
     */
    public Pagination<InventoryTransaction> getTransactionHistory(int productId, int page, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM dbo.inventory_transactions WHERE product_id = ?";
        int total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            countStmt.setInt(1, productId);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) total = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting inventory transactions for product: {}", productId, e);
            throw new DatabaseException("Error counting inventory transactions", e);
        }

        if (total == 0) {
            return new Pagination<>(new ArrayList<>(), page, pageSize, 0);
        }

        String dataSql = "SELECT it.transaction_id, it.product_id, it.previous_stock, it.quantity_changed, " +
                         "it.new_stock, it.transaction_type, it.reference_type, it.reference_id, it.remarks, " +
                         "it.created_by, it.created_at, p.product_name, p.sku, u.first_name, u.last_name " +
                         "FROM dbo.inventory_transactions it " +
                         "INNER JOIN dbo.products p ON it.product_id = p.product_id " +
                         "LEFT JOIN dbo.users u ON it.created_by = u.user_id " +
                         "WHERE it.product_id = ? " +
                         "ORDER BY it.created_at DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<InventoryTransaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(dataSql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, (page - 1) * pageSize);
            stmt.setInt(3, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching transactions for productId: {}", productId, e);
            throw new DatabaseException("Error fetching inventory transaction history", e);
        }

        return new Pagination<>(list, page, pageSize, total);
    }

    /**
     * Calculates summary metrics for administrative dashboard widgets.
     */
    public Map<String, Integer> getSummaryStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT " +
                     "COUNT(*) AS total_items, " +
                     "SUM(CASE WHEN quantity = 0 THEN 1 ELSE 0 END) AS out_of_stock, " +
                     "SUM(CASE WHEN quantity > 0 AND quantity <= low_stock_threshold THEN 1 ELSE 0 END) AS low_stock, " +
                     "SUM(CASE WHEN quantity > low_stock_threshold THEN 1 ELSE 0 END) AS in_stock, " +
                     "ISNULL(SUM(quantity), 0) AS total_units " +
                     "FROM dbo.inventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.put("totalItems", rs.getInt("total_items"));
                stats.put("outOfStock", rs.getInt("out_of_stock"));
                stats.put("lowStock", rs.getInt("low_stock"));
                stats.put("inStock", rs.getInt("in_stock"));
                stats.put("totalUnits", rs.getInt("total_units"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving inventory summary statistics", e);
        }
        return stats;
    }

    private void buildFilterConditions(String keyword, String filter, StringBuilder whereClause, List<Object> params) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.append("AND (p.product_name LIKE ? OR p.sku LIKE ? OR p.brand LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if ("low_stock".equalsIgnoreCase(filter)) {
            whereClause.append("AND i.quantity > 0 AND i.quantity <= i.low_stock_threshold ");
        } else if ("out_of_stock".equalsIgnoreCase(filter)) {
            whereClause.append("AND i.quantity = 0 ");
        } else if ("in_stock".equalsIgnoreCase(filter)) {
            whereClause.append("AND i.quantity > i.low_stock_threshold ");
        }
    }

    private int setStatementParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        int idx = 1;
        for (Object p : params) {
            stmt.setObject(idx++, p);
        }
        return idx;
    }

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setProductId(rs.getInt("product_id"));
        inv.setQuantity(rs.getInt("quantity"));
        inv.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        inv.setLastUpdated(rs.getTimestamp("last_updated") != null ? rs.getTimestamp("last_updated").toLocalDateTime() : null);
        inv.setProductName(rs.getString("product_name"));
        inv.setSku(rs.getString("sku"));
        inv.setBrand(rs.getString("brand"));
        inv.setPrice(rs.getBigDecimal("price"));
        inv.setCategoryName(rs.getString("category_name"));
        return inv;
    }

    private InventoryTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setProductId(rs.getInt("product_id"));
        t.setPreviousStock(rs.getInt("previous_stock"));
        t.setQuantityChanged(rs.getInt("quantity_changed"));
        t.setNewStock(rs.getInt("new_stock"));
        t.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        t.setReferenceType(rs.getString("reference_type"));
        int refId = rs.getInt("reference_id");
        if (!rs.wasNull()) t.setReferenceId(refId);
        t.setRemarks(rs.getString("remarks"));
        int createdBy = rs.getInt("created_by");
        if (!rs.wasNull()) t.setCreatedBy(createdBy);
        t.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        t.setProductName(rs.getString("product_name"));
        t.setSku(rs.getString("sku"));
        
        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        if (fn != null || ln != null) {
            t.setCreatedByName(((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim());
        } else {
            t.setCreatedByName("System / Customer");
        }
        return t;
    }
}
