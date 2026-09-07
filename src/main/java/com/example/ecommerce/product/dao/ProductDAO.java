package com.example.ecommerce.product.dao;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.model.ProductImage;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

import org.springframework.stereotype.Repository;

/**
 * Data Access Object for Product catalog management and SQL Server paginated
 * queries.
 */
@Repository
public class ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);

    /**
     * Executes dynamic search, filtering, and sorting with SQL Server OFFSET-FETCH
     * pagination.
     */
    public Pagination<Product> searchProducts(ProductSearchCriteria criteria) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildFilterConditions(criteria, whereClause, params);

        // 1. Get Total Records Count
        String countSql = "SELECT COUNT(*) FROM dbo.products p " +
                "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                whereClause;
        int totalRecords = 0;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement countStmt = conn.prepareStatement(countSql)) {
            setStatementParameters(countStmt, params);
            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) {
                    totalRecords = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting products for search criteria", e);
            throw new DatabaseException("Error counting products", e);
        }

        if (totalRecords == 0) {
            return new Pagination<>(new ArrayList<>(), criteria.getPage(), criteria.getPageSize(), 0);
        }

        // 2. Query Paginated Records
        String orderByColumn = switch (criteria.getSortBy()) {
            case "price" -> "p.price";
            case "product_name" -> "p.product_name";
            case "discount", "discount_percentage", "discount_desc", "deals" -> "p.discount_percentage";
            default -> "p.created_at";
        };
        String sortDir = "ASC".equalsIgnoreCase(criteria.getSortDirection()) ? "ASC" : "DESC";

        String dataSql = "SELECT p.product_id, p.category_id, p.sku, p.product_name, p.slug, p.description, " +
                "p.brand, p.price, p.discount_percentage, p.tax_percentage, p.weight_kg, p.status, " +
                "p.created_at, p.updated_at, c.category_name, c.slug AS category_slug, " +
                "COALESCE(i.quantity, 0) AS stock_quantity, COALESCE(i.low_stock_threshold, 5) AS low_stock_threshold, " +
                "COALESCE((SELECT AVG(r.rating) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0.0) AS avg_rating, " +
                "COALESCE((SELECT COUNT(*) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0) AS review_count " +
                "FROM dbo.products p " +
                "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                "LEFT JOIN dbo.inventory i ON p.product_id = i.product_id " +
                whereClause +
                " ORDER BY " + orderByColumn + " " + sortDir + " " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Product> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement dataStmt = conn.prepareStatement(dataSql)) {

            int paramIndex = setStatementParameters(dataStmt, params);
            dataStmt.setInt(paramIndex++, criteria.getOffset());
            dataStmt.setInt(paramIndex, criteria.getPageSize());

            try (ResultSet rs = dataStmt.executeQuery()) {
                while (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    product.setImages(getProductImages(product.getProductId(), conn));
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products with criteria", e);
            throw new DatabaseException("Error retrieving products", e);
        }

        return new Pagination<>(products, criteria.getPage(), criteria.getPageSize(), totalRecords);
    }

    /**
     * Retrieves single product by ID including category, stock, and gallery images.
     */
    public Optional<Product> findById(int productId) {
        String sql = "SELECT p.product_id, p.category_id, p.sku, p.product_name, p.slug, p.description, " +
                "p.brand, p.price, p.discount_percentage, p.tax_percentage, p.weight_kg, p.status, " +
                "p.created_at, p.updated_at, c.category_name, c.slug AS category_slug, " +
                "COALESCE(i.quantity, 0) AS stock_quantity, COALESCE(i.low_stock_threshold, 5) AS low_stock_threshold, " +
                "COALESCE((SELECT AVG(r.rating) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0.0) AS avg_rating, " +
                "COALESCE((SELECT COUNT(*) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0) AS review_count " +
                "FROM dbo.products p " +
                "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                "LEFT JOIN dbo.inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    product.setImages(getProductImages(product.getProductId(), conn));
                    return Optional.of(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", productId, e);
            throw new DatabaseException("Error retrieving product by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves single product by URL slug.
     */
    public Optional<Product> findBySlug(String slug) {
        String sql = "SELECT p.product_id, p.category_id, p.sku, p.product_name, p.slug, p.description, " +
                "p.brand, p.price, p.discount_percentage, p.tax_percentage, p.weight_kg, p.status, " +
                "p.created_at, p.updated_at, c.category_name, c.slug AS category_slug, " +
                "COALESCE(i.quantity, 0) AS stock_quantity, COALESCE(i.low_stock_threshold, 5) AS low_stock_threshold, " +
                "COALESCE((SELECT AVG(r.rating) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0.0) AS avg_rating, " +
                "COALESCE((SELECT COUNT(*) FROM dbo.reviews r WHERE r.product_id = p.product_id AND (r.status = 'APPROVED' OR r.status IS NULL)), 0) AS review_count " +
                "FROM dbo.products p " +
                "INNER JOIN dbo.categories c ON p.category_id = c.category_id " +
                "LEFT JOIN dbo.inventory i ON p.product_id = i.product_id " +
                "WHERE p.slug = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, slug.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    product.setImages(getProductImages(product.getProductId(), conn));
                    return Optional.of(product);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding product by slug: {}", slug, e);
            throw new DatabaseException("Error retrieving product by slug", e);
        }
        return Optional.empty();
    }

    /**
     * Checks if SKU already exists in database.
     */
    public boolean existsBySku(String sku, Integer excludeProductId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM dbo.products WHERE LOWER(sku) = LOWER(?) ");
        if (excludeProductId != null) {
            sql.append("AND product_id != ?");
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, sku.trim());
            if (excludeProductId != null) {
                stmt.setInt(2, excludeProductId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking SKU uniqueness for: {}", sku, e);
            throw new DatabaseException("Error checking SKU uniqueness", e);
        }
    }

    /**
     * Checks if Slug already exists in database.
     */
    public boolean existsBySlug(String slug, Integer excludeProductId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM dbo.products WHERE LOWER(slug) = LOWER(?) ");
        if (excludeProductId != null) {
            sql.append("AND product_id != ?");
        }
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, slug.trim());
            if (excludeProductId != null) {
                stmt.setInt(2, excludeProductId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking slug uniqueness for: {}", slug, e);
            throw new DatabaseException("Error checking product slug uniqueness", e);
        }
    }

    /**
     * Inserts product record within an active transaction connection.
     */
    public int createProduct(Product product, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.products (category_id, sku, product_name, slug, description, brand, " +
                "price, discount_percentage, tax_percentage, weight_kg, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getSku().trim().toUpperCase());
            stmt.setString(3, product.getProductName().trim());
            stmt.setString(4, product.getSlug().trim());
            stmt.setString(5, product.getDescription());
            stmt.setString(6, product.getBrand());
            stmt.setBigDecimal(7, product.getPrice());
            stmt.setBigDecimal(8, product.getDiscountPercentage());
            stmt.setBigDecimal(9, product.getTaxPercentage());
            stmt.setBigDecimal(10, product.getWeightKg());
            stmt.setString(11, product.getStatus());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    product.setProductId(id);
                    return id;
                }
            }
            throw new SQLException("Failed to create product, no ID generated.");
        }
    }

    /**
     * Updates an existing product within transaction.
     */
    public boolean updateProduct(Product product, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.products SET category_id = ?, sku = ?, product_name = ?, slug = ?, " +
                "description = ?, brand = ?, price = ?, discount_percentage = ?, tax_percentage = ?, " +
                "weight_kg = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCategoryId());
            stmt.setString(2, product.getSku().trim().toUpperCase());
            stmt.setString(3, product.getProductName().trim());
            stmt.setString(4, product.getSlug().trim());
            stmt.setString(5, product.getDescription());
            stmt.setString(6, product.getBrand());
            stmt.setBigDecimal(7, product.getPrice());
            stmt.setBigDecimal(8, product.getDiscountPercentage());
            stmt.setBigDecimal(9, product.getTaxPercentage());
            stmt.setBigDecimal(10, product.getWeightKg());
            stmt.setString(11, product.getStatus());
            stmt.setInt(12, product.getProductId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates product status (e.g. ACTIVE -> INACTIVE or ARCHIVED).
     */
    public boolean updateStatus(int productId, String status) {
        String sql = "UPDATE dbo.products SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for productId: {}", productId, e);
            throw new DatabaseException("Error updating product status", e);
        }
    }

    /**
     * Retrieves all product images for a product.
     */
    public List<ProductImage> getProductImages(int productId, Connection conn) throws SQLException {
        String sql = "SELECT image_id, product_id, image_url, alt_text, display_order, is_primary, created_at " +
                "FROM dbo.product_images WHERE product_id = ? ORDER BY is_primary DESC, display_order ASC";
        List<ProductImage> images = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(new ProductImage(
                            rs.getInt("image_id"),
                            rs.getInt("product_id"),
                            rs.getString("image_url"),
                            rs.getString("alt_text"),
                            rs.getInt("display_order"),
                            rs.getBoolean("is_primary")));
                }
            }
        }
        return images;
    }

    /**
     * Saves product image record in transaction.
     */
    public void saveProductImage(ProductImage image, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.product_images (product_id, image_url, alt_text, display_order, is_primary, created_at) "
                +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, image.getProductId());
            stmt.setString(2, image.getImageUrl().trim());
            stmt.setString(3, image.getAltText());
            stmt.setInt(4, image.getDisplayOrder());
            stmt.setBoolean(5, image.isPrimary());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes existing gallery images for a product before replacement.
     */
    public void deleteProductImages(int productId, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.product_images WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    /**
     * Initializes inventory row for a newly created product.
     */
    public void initProductInventory(int productId, int quantity, int lowStockThreshold, Connection conn)
            throws SQLException {
        String sql = "INSERT INTO dbo.inventory (product_id, quantity, low_stock_threshold, last_updated) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, Math.max(0, quantity));
            stmt.setInt(3, Math.max(0, lowStockThreshold));
            stmt.executeUpdate();
        }

        if (quantity > 0) {
            String transSql = "INSERT INTO dbo.inventory_transactions (product_id, previous_stock, quantity_changed, " +
                    "new_stock, transaction_type, reference_type, remarks, created_at) " +
                    "VALUES (?, 0, ?, ?, 'PURCHASE', 'INITIAL_STOCK', 'Product catalog onboarding', CURRENT_TIMESTAMP)";
            try (PreparedStatement transStmt = conn.prepareStatement(transSql)) {
                transStmt.setInt(1, productId);
                transStmt.setInt(2, quantity);
                transStmt.setInt(3, quantity);
                transStmt.executeUpdate();
            }
        }
    }

    private void buildFilterConditions(ProductSearchCriteria criteria, StringBuilder whereClause, List<Object> params) {
        if (criteria.getStatus() != null && !criteria.getStatus().trim().isEmpty()) {
            whereClause.append("AND p.status = ? ");
            params.add(criteria.getStatus());
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            whereClause
                    .append("AND (p.product_name LIKE ? OR p.description LIKE ? OR p.brand LIKE ? OR p.sku LIKE ?) ");
            String kwParam = "%" + criteria.getKeyword().trim() + "%";
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
            params.add(kwParam);
        }

        if (criteria.getCategoryId() != null && criteria.getCategoryId() > 0) {
            whereClause.append("AND (p.category_id = ? OR c.parent_category_id = ?) ");
            params.add(criteria.getCategoryId());
            params.add(criteria.getCategoryId());
        }

        // Multiple price ranges selection
        if (criteria.getPriceRanges() != null && !criteria.getPriceRanges().isEmpty()) {
            StringBuilder priceOr = new StringBuilder("AND ( ");
            boolean first = true;
            for (String range : criteria.getPriceRanges()) {
                if (range == null || range.trim().isEmpty()) continue;
                if (!first) priceOr.append(" OR ");
                first = false;
                switch (range.trim().toLowerCase()) {
                    case "below_15k", "below_15000" -> priceOr.append("p.price < 15000");
                    case "15k_30k", "15000_30000" -> priceOr.append("(p.price >= 15000 AND p.price < 30000)");
                    case "30k_40k", "30000_40000" -> priceOr.append("(p.price >= 30000 AND p.price < 40000)");
                    case "40k_50k", "40000_50000" -> priceOr.append("(p.price >= 40000 AND p.price < 50000)");
                    case "50k_1lakh", "50000_100000" -> priceOr.append("(p.price >= 50000 AND p.price < 100000)");
                    case "above_1lakh", "above_100000", "1lakh_above" -> priceOr.append("p.price >= 100000");
                    default -> priceOr.append("1=1");
                }
            }
            priceOr.append(" ) ");
            if (!first) {
                whereClause.append(priceOr);
            }
        }

        // Custom min/max price overrides
        if (criteria.getMinPrice() != null) {
            whereClause.append("AND p.price >= ? ");
            params.add(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            whereClause.append("AND p.price <= ? ");
            params.add(criteria.getMaxPrice());
        }

        // Discount / Deals filter
        if (criteria.getMinDiscount() != null && criteria.getMinDiscount().compareTo(BigDecimal.ZERO) > 0) {
            whereClause.append("AND p.discount_percentage >= ? ");
            params.add(criteria.getMinDiscount());
        } else if (criteria.isDealsOnly()) {
            whereClause.append("AND p.discount_percentage > 0 ");
        }

        // Multi-brand or single brand filter
        if (criteria.getBrands() != null && !criteria.getBrands().isEmpty()) {
            whereClause.append("AND LOWER(p.brand) IN (");
            for (int i = 0; i < criteria.getBrands().size(); i++) {
                if (i > 0) whereClause.append(", ");
                whereClause.append("LOWER(?)");
                params.add(criteria.getBrands().get(i));
            }
            whereClause.append(") ");
        } else if (criteria.getBrand() != null && !criteria.getBrand().trim().isEmpty()) {
            whereClause.append("AND LOWER(p.brand) = LOWER(?) ");
            params.add(criteria.getBrand().trim());
        }
    }

    /**
     * Retrieves distinct brand names, optionally scoped to a specific category or root category hierarchy.
     */
    public List<String> findDistinctBrandsByCategory(Integer categoryId) {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT p.brand FROM dbo.products p " +
            "LEFT JOIN dbo.categories c ON p.category_id = c.category_id " +
            "WHERE p.status = 'ACTIVE' AND p.brand IS NOT NULL AND p.brand <> '' "
        );
        List<Object> params = new ArrayList<>();
        if (categoryId != null && categoryId > 0) {
            sql.append("AND (p.category_id = ? OR c.parent_category_id = ?) ");
            params.add(categoryId);
            params.add(categoryId);
        }
        sql.append("ORDER BY p.brand ASC");

        List<String> brands = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String b = rs.getString("brand");
                    if (b != null && !b.trim().isEmpty() && !brands.contains(b.trim())) {
                        brands.add(b.trim());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct brands for categoryId: {}", categoryId, e);
        }
        return brands;
    }

    /**
     * Returns a map of category_id -> List of distinct brand names for instant client-side dynamic rendering.
     */
    public Map<Integer, List<String>> findCategoryBrandsMap() {
        String sql = "SELECT DISTINCT p.category_id, c.parent_category_id, p.brand " +
                     "FROM dbo.products p " +
                     "LEFT JOIN dbo.categories c ON p.category_id = c.category_id " +
                     "WHERE p.status = 'ACTIVE' AND p.brand IS NOT NULL AND p.brand <> '' " +
                     "ORDER BY p.brand ASC";

        Map<Integer, List<String>> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int catId = rs.getInt("category_id");
                int parentCatId = rs.getInt("parent_category_id");
                String brand = rs.getString("brand");
                if (brand != null && !brand.trim().isEmpty()) {
                    brand = brand.trim();
                    map.computeIfAbsent(catId, k -> new ArrayList<>());
                    if (!map.get(catId).contains(brand)) {
                        map.get(catId).add(brand);
                    }
                    if (parentCatId > 0) {
                        map.computeIfAbsent(parentCatId, k -> new ArrayList<>());
                        if (!map.get(parentCatId).contains(brand)) {
                            map.get(parentCatId).add(brand);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching category-brand mapping", e);
        }
        return map;
    }

    private int setStatementParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            if (param instanceof String s) {
                stmt.setString(index++, s);
            } else if (param instanceof Integer i) {
                stmt.setInt(index++, i);
            } else if (param instanceof BigDecimal bd) {
                stmt.setBigDecimal(index++, bd);
            } else {
                stmt.setObject(index++, param);
            }
        }
        return index;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setSku(rs.getString("sku"));
        p.setProductName(rs.getString("product_name"));
        p.setSlug(rs.getString("slug"));
        p.setDescription(rs.getString("description"));
        p.setBrand(rs.getString("brand"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        p.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
        p.setWeightKg(rs.getBigDecimal("weight_kg"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        p.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setLowStockThreshold(rs.getInt("low_stock_threshold"));

        try {
            p.setAverageRating(rs.getDouble("avg_rating"));
            p.setReviewCount(rs.getInt("review_count"));
        } catch (SQLException ignore) {}

        Category category = new Category();
        category.setCategoryId(p.getCategoryId());
        category.setCategoryName(rs.getString("category_name"));
        category.setSlug(rs.getString("category_slug"));
        p.setCategory(category);

        return p;
    }
}
