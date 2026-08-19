package com.example.ecommerce.cart.dao;

import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for Shopping Cart operations in Microsoft SQL Server.
 */
public class CartDAO {

    private static final Logger logger = LoggerFactory.getLogger(CartDAO.class);

    /**
     * Retrieves existing cart ID or provisions a new cart for the user.
     */
    public int getOrCreateCartId(int userId, Connection conn) throws SQLException {
        String findSql = "SELECT cart_id FROM dbo.carts WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_id");
                }
            }
        }

        // Insert new cart if none exists
        String insertSql = "INSERT INTO dbo.carts (user_id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to resolve or create cart for userId: " + userId);
    }

    /**
     * Retrieves full cart populated with live product metadata, prices, discounts, and inventory balances.
     */
    public Optional<Cart> getCartWithItems(int userId) {
        String cartSql = "SELECT cart_id, user_id, created_at, updated_at FROM dbo.carts WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection()) {
            int cartId = 0;
            Cart cart = null;

            try (PreparedStatement stmt = conn.prepareStatement(cartSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        cartId = rs.getInt("cart_id");
                        cart = new Cart(cartId, userId);
                        cart.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                        cart.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                    }
                }
            }

            if (cart == null) {
                // Auto create empty cart
                cartId = getOrCreateCartId(userId, conn);
                cart = new Cart(cartId, userId);
            }

            // Query items joined with live catalog pricing and stock
            String itemsSql = "SELECT ci.cart_item_id, ci.cart_id, ci.product_id, ci.quantity, ci.created_at, ci.updated_at, " +
                              "p.product_name, p.sku, p.brand, p.price, p.discount_percentage, p.status AS product_status, " +
                              "COALESCE(i.quantity, 0) AS stock_quantity, " +
                              "(SELECT image_url FROM dbo.product_images pi WHERE pi.product_id = p.product_id ORDER BY pi.is_primary DESC, pi.display_order ASC LIMIT 1) AS primary_image_url " +
                              "FROM dbo.cart_items ci " +
                              "INNER JOIN dbo.products p ON ci.product_id = p.product_id " +
                              "LEFT JOIN dbo.inventory i ON p.product_id = i.product_id " +
                              "WHERE ci.cart_id = ? " +
                              "ORDER BY ci.created_at ASC";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemsSql)) {
                itemStmt.setInt(1, cartId);
                try (ResultSet rs = itemStmt.executeQuery()) {
                    while (rs.next()) {
                        CartItem item = new CartItem();
                        item.setCartItemId(rs.getInt("cart_item_id"));
                        item.setCartId(rs.getInt("cart_id"));
                        item.setProductId(rs.getInt("product_id"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                        item.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
                        
                        // Live Authoritative Catalog Data
                        item.setProductName(rs.getString("product_name"));
                        item.setSku(rs.getString("sku"));
                        item.setBrand(rs.getString("brand"));
                        item.setPrice(rs.getBigDecimal("price"));
                        item.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
                        item.setProductStatus(rs.getString("product_status"));
                        item.setStockQuantity(rs.getInt("stock_quantity"));
                        item.setPrimaryImageUrl(rs.getString("primary_image_url"));

                        cart.addItem(item);
                    }
                }
            }

            return Optional.of(cart);

        } catch (SQLException e) {
            logger.error("Error retrieving shopping cart for userId: {}", userId, e);
            throw new DatabaseException("Error retrieving shopping cart", e);
        }
    }

    /**
     * Upserts an item into the cart (increments quantity if already in cart).
     */
    public void upsertCartItem(int cartId, int productId, int quantityToAdd) {
        String updateSql = "UPDATE dbo.cart_items SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP " +
                           "WHERE cart_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO dbo.cart_items (cart_id, product_id, quantity, created_at, updated_at) " +
                           "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DBConnection.getConnection()) {
            int rowsUpdated = 0;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, quantityToAdd);
                updateStmt.setInt(2, cartId);
                updateStmt.setInt(3, productId);
                rowsUpdated = updateStmt.executeUpdate();
            }

            if (rowsUpdated == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, cartId);
                    insertStmt.setInt(2, productId);
                    insertStmt.setInt(3, quantityToAdd);
                    insertStmt.executeUpdate();
                }
            }
            updateCartTimestamp(cartId, conn);
        } catch (SQLException e) {
            logger.error("Error adding product {} to cart {}", productId, cartId, e);
            throw new DatabaseException("Error adding item to shopping cart", e);
        }
    }

    /**
     * Sets exact quantity for a cart item.
     */
    public void updateItemQuantity(int cartId, int productId, int newQuantity) {
        String sql = "UPDATE dbo.cart_items SET quantity = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, cartId);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
            updateCartTimestamp(cartId, conn);
        } catch (SQLException e) {
            logger.error("Error updating quantity for cart {} product {}", cartId, productId, e);
            throw new DatabaseException("Error updating item quantity", e);
        }
    }

    /**
     * Removes an item from the cart.
     */
    public void removeItem(int cartId, int productId) {
        String sql = "DELETE FROM dbo.cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
            updateCartTimestamp(cartId, conn);
        } catch (SQLException e) {
            logger.error("Error removing product {} from cart {}", productId, cartId, e);
            throw new DatabaseException("Error removing item from cart", e);
        }
    }

    /**
     * Empties all items from the cart.
     */
    public void clearCart(int cartId, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.cart_items WHERE cart_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
            updateCartTimestamp(cartId, conn);
        }
    }

    /**
     * Gets the total count of items in the user's cart for header badge display.
     */
    public int getItemCount(int userId) {
        String sql = "SELECT COALESCE(SUM(ci.quantity), 0) " +
                     "FROM dbo.cart_items ci " +
                     "INNER JOIN dbo.carts c ON ci.cart_id = c.cart_id " +
                     "WHERE c.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting cart items for userId: {}", userId, e);
        }
        return 0;
    }

    private void updateCartTimestamp(int cartId, Connection conn) {
        String sql = "UPDATE dbo.carts SET updated_at = CURRENT_TIMESTAMP WHERE cart_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
