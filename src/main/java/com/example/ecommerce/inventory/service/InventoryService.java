package com.example.ecommerce.inventory.service;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.dao.InventoryDAO;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.inventory.model.InventoryTransaction;
import com.example.ecommerce.inventory.model.TransactionType;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Service Layer enforcing strict inventory accounting, row-level locking concurrency protection,
 * and immutable stock transaction logging.
 */
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryDAO inventoryDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
    }

    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    public Pagination<Inventory> getInventoryList(String keyword, String filter, int page, int pageSize) {
        return inventoryDAO.findAll(keyword, filter, page, pageSize);
    }

    public Inventory getProductInventory(int productId) {
        return inventoryDAO.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
    }

    public Map<String, Integer> getSummaryStats() {
        return inventoryDAO.getSummaryStats();
    }

    public Pagination<InventoryTransaction> getTransactionHistory(int productId, int page, int pageSize) {
        return inventoryDAO.getTransactionHistory(productId, page, pageSize);
    }

    /**
     * Admin Restock Operation (Transactional).
     * Increases stock level and records a PURCHASE transaction.
     */
    public void restockProduct(int productId, int additionalQuantity, String referenceType, 
                               Integer referenceId, String remarks, Integer adminUserId) {
        if (additionalQuantity <= 0) {
            throw new ValidationException("Restock quantity must be greater than 0.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                // 1. Lock Row
                Inventory current = inventoryDAO.getInventoryWithLock(productId, conn)
                        .orElseThrow(() -> new ResourceNotFoundException("Product inventory record not found"));

                int previousStock = current.getQuantity();
                int newStock = previousStock + additionalQuantity;

                // 2. Update Stock
                inventoryDAO.updateStock(productId, newStock, conn);

                // 3. Record Audit Transaction
                InventoryTransaction trans = new InventoryTransaction(
                        productId,
                        previousStock,
                        additionalQuantity,
                        newStock,
                        TransactionType.PURCHASE,
                        referenceType != null ? referenceType : "SUPPLIER_RESTOCK",
                        referenceId,
                        remarks != null ? remarks : "Restocked " + additionalQuantity + " units",
                        adminUserId
                );
                inventoryDAO.recordTransaction(trans, conn);

                conn.commit();
                logger.info("Restocked product [id={}] with +{} units. New stock: {}", productId, additionalQuantity, newStock);

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error during restock transaction for productId: {}", productId, e);
                throw new DatabaseException("Failed to restock product", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during restock", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    /**
     * Admin Manual Adjustment Operation (Transactional).
     * Corrects physical stock count (e.g. following warehouse audits or damages).
     */
    public void adjustStock(int productId, int newQuantity, String remarks, Integer adminUserId) {
        if (newQuantity < 0) {
            throw new ValidationException("Stock quantity cannot be negative.");
        }
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new ValidationException("Adjustment reason/remarks are mandatory for audit compliance.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                Inventory current = inventoryDAO.getInventoryWithLock(productId, conn)
                        .orElseThrow(() -> new ResourceNotFoundException("Product inventory record not found"));

                int previousStock = current.getQuantity();
                int delta = newQuantity - previousStock;

                if (delta == 0) {
                    conn.rollback();
                    return; // No change needed
                }

                // Update Stock
                inventoryDAO.updateStock(productId, newQuantity, conn);

                // Record Audit Transaction
                InventoryTransaction trans = new InventoryTransaction(
                        productId,
                        previousStock,
                        delta,
                        newQuantity,
                        TransactionType.ADJUSTMENT,
                        "AUDIT_CORRECTION",
                        null,
                        remarks.trim(),
                        adminUserId
                );
                inventoryDAO.recordTransaction(trans, conn);

                conn.commit();
                logger.info("Adjusted stock for product [id={}]: {} -> {} (delta: {})", productId, previousStock, newQuantity, delta);

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error during stock adjustment for productId: {}", productId, e);
                throw new DatabaseException("Failed to adjust inventory count", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during adjustment", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    /**
     * Deducts stock during Order Placement within the active Order Transaction.
     * Uses UPDLOCK and ROWLOCK to prevent race conditions and negative inventory.
     */
    public void reserveStockForOrder(int productId, int quantityToDeduct, int orderId, Connection conn) throws SQLException {
        if (quantityToDeduct <= 0) {
            throw new ValidationException("Deduction quantity must be positive.");
        }

        Inventory current = inventoryDAO.getInventoryWithLock(productId, conn)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in inventory: " + productId));

        if (current.getQuantity() < quantityToDeduct) {
            throw new ValidationException("Insufficient stock for product '" + current.getProductName() + 
                    "'. Requested: " + quantityToDeduct + ", Available: " + current.getQuantity());
        }

        int previousStock = current.getQuantity();
        int newStock = previousStock - quantityToDeduct;

        inventoryDAO.updateStock(productId, newStock, conn);

        InventoryTransaction trans = new InventoryTransaction(
                productId,
                previousStock,
                -quantityToDeduct,
                newStock,
                TransactionType.SALE,
                "ORDER",
                orderId,
                "Customer Order #" + orderId,
                null
        );
        inventoryDAO.recordTransaction(trans, conn);
    }

    /**
     * Restores stock when an Order is Cancelled or Returned.
     */
    public void restoreStockForCancellation(int productId, int quantityToRestore, int orderId, 
                                            TransactionType type, String remarks, Connection conn) throws SQLException {
        Inventory current = inventoryDAO.getInventoryWithLock(productId, conn)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in inventory: " + productId));

        int previousStock = current.getQuantity();
        int newStock = previousStock + quantityToRestore;

        inventoryDAO.updateStock(productId, newStock, conn);

        InventoryTransaction trans = new InventoryTransaction(
                productId,
                previousStock,
                quantityToRestore,
                newStock,
                type != null ? type : TransactionType.CANCELLATION,
                "ORDER_CANCELLATION",
                orderId,
                remarks != null ? remarks : "Restored stock from cancelled Order #" + orderId,
                null
        );
        inventoryDAO.recordTransaction(trans, conn);
    }

    public void updateLowStockThreshold(int productId, int threshold) {
        if (threshold < 0) {
            throw new ValidationException("Threshold cannot be negative.");
        }
        inventoryDAO.updateThreshold(productId, threshold);
    }
}
