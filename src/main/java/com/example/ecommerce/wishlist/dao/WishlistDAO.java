package com.example.ecommerce.wishlist.dao;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.wishlist.model.Wishlist;
import com.example.ecommerce.wishlist.model.WishlistItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/**
 * Data Access Object for customer Wishlists in Microsoft SQL Server.
 */
@Repository
public class WishlistDAO {

    private static final Logger logger = LoggerFactory.getLogger(WishlistDAO.class);

    public int getOrCreateWishlistId(int userId, Connection conn) throws SQLException {
        String findSql = "SELECT wishlist_id FROM dbo.wishlists WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("wishlist_id");
                }
            }
        }

        String insertSql = "INSERT INTO dbo.wishlists (user_id, created_at) VALUES (?, CURRENT_TIMESTAMP)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, userId);
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to resolve or create wishlist for userId: " + userId);
    }

    public Optional<com.example.ecommerce.wishlist.model.Wishlist> getWishlistWithItems(int userId) {
        String wishlistSql = "SELECT wishlist_id, user_id, created_at FROM dbo.wishlists WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            int wishlistId = 0;
            Wishlist wishlist = null;

            try (PreparedStatement stmt = conn.prepareStatement(wishlistSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        wishlistId = rs.getInt("wishlist_id");
                        wishlist = new Wishlist(wishlistId, userId);
                        wishlist.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
                    }
                }
            }

            if (wishlist == null) {
                wishlistId = getOrCreateWishlistId(userId, conn);
                wishlist = new Wishlist(wishlistId, userId);
            }

            String itemsSql = "SELECT wi.wishlist_item_id, wi.wishlist_id, wi.product_id, wi.added_at, " +
                              "p.product_name, p.sku, p.brand, p.price, p.discount_percentage, " +
                              "COALESCE(i.quantity, 0) AS stock_quantity, " +
                              "(SELECT image_url FROM dbo.product_images pi WHERE pi.product_id = p.product_id ORDER BY pi.is_primary DESC, pi.display_order ASC LIMIT 1) AS primary_image_url " +
                              "FROM dbo.wishlist_items wi " +
                              "INNER JOIN dbo.products p ON wi.product_id = p.product_id " +
                              "LEFT JOIN dbo.inventory i ON p.product_id = i.product_id " +
                              "WHERE wi.wishlist_id = ? " +
                              "ORDER BY wi.added_at DESC";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemsSql)) {
                itemStmt.setInt(1, wishlistId);
                try (ResultSet rs = itemStmt.executeQuery()) {
                    while (rs.next()) {
                        WishlistItem item = new WishlistItem();
                        item.setWishlistItemId(rs.getInt("wishlist_item_id"));
                        item.setWishlistId(rs.getInt("wishlist_id"));
                        item.setProductId(rs.getInt("product_id"));
                        item.setCreatedAt(rs.getTimestamp("added_at") != null ? rs.getTimestamp("added_at").toLocalDateTime() : null);
                        item.setProductName(rs.getString("product_name"));
                        item.setSku(rs.getString("sku"));
                        item.setBrand(rs.getString("brand"));
                        item.setPrice(rs.getBigDecimal("price"));
                        item.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
                        item.setStockQuantity(rs.getInt("stock_quantity"));
                        item.setPrimaryImageUrl(rs.getString("primary_image_url"));

                        wishlist.addItem(item);
                    }
                }
            }

            return Optional.of(wishlist);

        } catch (SQLException e) {
            logger.error("Error retrieving wishlist for userId: {}", userId, e);
            throw new DatabaseException("Error retrieving wishlist", e);
        }
    }

    public void addItem(int wishlistId, int productId) {
        String sql = "INSERT INTO dbo.wishlist_items (wishlist_id, product_id, added_at) " +
                     "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                     "ON CONFLICT (wishlist_id, product_id) DO NOTHING";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding product {} to wishlist {}", productId, wishlistId, e);
            throw new DatabaseException("Error adding item to wishlist", e);
        }
    }

    public void removeItem(int wishlistId, int productId) {
        String sql = "DELETE FROM dbo.wishlist_items WHERE wishlist_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error removing product {} from wishlist {}", productId, wishlistId, e);
            throw new DatabaseException("Error removing item from wishlist", e);
        }
    }

    public void removeItemByUserId(int userId, int productId) {
        String sql = "DELETE FROM dbo.wishlist_items " +
                     "WHERE wishlist_id IN (SELECT wishlist_id FROM dbo.wishlists WHERE user_id = ?) " +
                     "AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error removing product {} from wishlist for userId {}", productId, userId, e);
            throw new DatabaseException("Error removing item from wishlist", e);
        }
    }

    public boolean isInWishlist(int userId, int productId) {
        String sql = "SELECT 1 FROM dbo.wishlist_items wi " +
                     "INNER JOIN dbo.wishlists w ON wi.wishlist_id = w.wishlist_id " +
                     "WHERE w.user_id = ? AND wi.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking wishlist membership for userId: {}, productId: {}", userId, productId, e);
        }
        return false;
    }

    public int getItemCount(int userId) {
        String sql = "SELECT COUNT(*) FROM dbo.wishlist_items wi " +
                     "INNER JOIN dbo.wishlists w ON wi.wishlist_id = w.wishlist_id " +
                     "WHERE w.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting wishlist items for userId: {}", userId, e);
        }
        return 0;
    }
}
