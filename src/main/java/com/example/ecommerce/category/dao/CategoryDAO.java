package com.example.ecommerce.category.dao;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO responsible for category queries and hierarchical relationship mapping in SQL Server.
 */
public class CategoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    /**
     * Retrieves all categories, optionally filtering only active categories.
     */
    public List<Category> findAll(boolean activeOnly) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.category_id, c.parent_category_id, c.category_name, c.slug, c.description, c.is_active, " +
                "c.created_at, c.updated_at, p.category_name AS parent_name " +
                "FROM dbo.categories c " +
                "LEFT JOIN dbo.categories p ON c.parent_category_id = p.category_id "
        );
        if (activeOnly) {
            sql.append("WHERE c.is_active = true ");
        }
        sql.append("ORDER BY COALESCE(c.parent_category_id, c.category_id), c.category_id ASC");

        List<Category> categories = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving categories list", e);
            throw new DatabaseException("Error retrieving categories", e);
        }
        return categories;
    }

    /**
     * Finds category by its primary key ID.
     */
    public Optional<Category> findById(int categoryId) {
        String sql = "SELECT c.category_id, c.parent_category_id, c.category_name, c.slug, c.description, c.is_active, " +
                     "c.created_at, c.updated_at, p.category_name AS parent_name " +
                     "FROM dbo.categories c " +
                     "LEFT JOIN dbo.categories p ON c.parent_category_id = p.category_id " +
                     "WHERE c.category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by ID: {}", categoryId, e);
            throw new DatabaseException("Error retrieving category by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds category by its unique URL slug.
     */
    public Optional<Category> findBySlug(String slug) {
        String sql = "SELECT c.category_id, c.parent_category_id, c.category_name, c.slug, c.description, c.is_active, " +
                     "c.created_at, c.updated_at, p.category_name AS parent_name " +
                     "FROM dbo.categories c " +
                     "LEFT JOIN dbo.categories p ON c.parent_category_id = p.category_id " +
                     "WHERE c.slug = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, slug.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by slug: {}", slug, e);
            throw new DatabaseException("Error retrieving category by slug", e);
        }
        return Optional.empty();
    }

    /**
     * Checks if slug already exists, excluding a specific categoryId when updating.
     */
    public boolean existsBySlug(String slug, Integer excludeCategoryId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM dbo.categories WHERE LOWER(slug) = LOWER(?) ");
        if (excludeCategoryId != null) {
            sql.append("AND category_id != ?");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, slug.trim());
            if (excludeCategoryId != null) {
                stmt.setInt(2, excludeCategoryId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking slug existence for category: {}", slug, e);
            throw new DatabaseException("Error checking category slug", e);
        }
    }

    /**
     * Inserts a new category into dbo.categories.
     */
    public int createCategory(Category category) {
        String sql = "INSERT INTO dbo.categories (parent_category_id, category_name, slug, description, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (category.getParentCategoryId() != null && category.getParentCategoryId() > 0) {
                stmt.setInt(1, category.getParentCategoryId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, category.getCategoryName().trim());
            stmt.setString(3, category.getSlug().trim());
            stmt.setString(4, category.getDescription() != null ? category.getDescription().trim() : null);
            stmt.setBoolean(5, category.isActive());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    category.setCategoryId(generatedId);
                    return generatedId;
                }
            }
            throw new SQLException("Creating category failed, no ID obtained.");
        } catch (SQLException e) {
            logger.error("Error creating category: {}", category.getCategoryName(), e);
            throw new DatabaseException("Error creating category", e);
        }
    }

    /**
     * Updates an existing category.
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE dbo.categories SET parent_category_id = ?, category_name = ?, slug = ?, " +
                     "description = ?, is_active = ?, updated_at = SYSDATETIME() WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (category.getParentCategoryId() != null && category.getParentCategoryId() > 0) {
                stmt.setInt(1, category.getParentCategoryId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, category.getCategoryName().trim());
            stmt.setString(3, category.getSlug().trim());
            stmt.setString(4, category.getDescription() != null ? category.getDescription().trim() : null);
            stmt.setBoolean(5, category.isActive());
            stmt.setInt(6, category.getCategoryId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating category ID: {}", category.getCategoryId(), e);
            throw new DatabaseException("Error updating category", e);
        }
    }

    /**
     * Updates active status (activation/deactivation).
     */
    public boolean updateStatus(int categoryId, boolean active) {
        String sql = "UPDATE dbo.categories SET is_active = ?, updated_at = SYSDATETIME() WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for category ID: {}", categoryId, e);
            throw new DatabaseException("Error updating category status", e);
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        int parentId = rs.getInt("parent_category_id");
        if (!rs.wasNull()) {
            category.setParentCategoryId(parentId);
        }
        category.setCategoryName(rs.getString("category_name"));
        category.setSlug(rs.getString("slug"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        category.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        category.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        category.setParentCategoryName(rs.getString("parent_name"));
        return category;
    }
}
