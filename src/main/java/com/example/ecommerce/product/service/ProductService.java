package com.example.ecommerce.product.service;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.model.ProductImage;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service Layer coordinating product catalog operations, pricing validation,
 * image associations, and inventory transaction onboarding.
 */
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /**
     * Searches and filters products with pagination.
     */
    public Pagination<Product> searchCatalog(ProductSearchCriteria criteria) {
        return productDAO.searchProducts(criteria);
    }

    /**
     * Retrieves all distinct active brand names for a category hierarchy or entire catalog.
     */
    public List<String> getDistinctBrands(Integer categoryId) {
        return productDAO.findDistinctBrandsByCategory(categoryId);
    }

    /**
     * Returns category -> distinct brand list mapping for dynamic client-side filtering.
     */
    public Map<Integer, List<String>> getCategoryBrandsMap() {
        return productDAO.findCategoryBrandsMap();
    }

    /**
     * Retrieves product by ID.
     */
    public Product getProductById(int productId) {
        return productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    /**
     * Retrieves product by unique URL slug.
     */
    public Product getProductBySlug(String slug) {
        return productDAO.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
    }

    /**
     * Creates a new product with images and initial inventory in a single atomic transaction.
     */
    public Product createProduct(Product product, List<ProductImage> images, int initialStock, int lowStockThreshold) {
        validateProduct(product, null);

        // SKU Uniqueness
        if (productDAO.existsBySku(product.getSku(), null)) {
            throw new ValidationException("A product with SKU '" + product.getSku() + "' already exists.");
        }

        // Slug Generation & Uniqueness
        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            product.setSlug(generateSlug(product.getProductName()));
        }
        if (productDAO.existsBySlug(product.getSlug(), null)) {
            product.setSlug(product.getSlug() + "-" + System.currentTimeMillis() % 10000);
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                // 1. Insert Product
                int productId = productDAO.createProduct(product, conn);

                // 2. Insert Images
                if (images != null && !images.isEmpty()) {
                    for (ProductImage img : images) {
                        img.setProductId(productId);
                        productDAO.saveProductImage(img, conn);
                    }
                }

                // 3. Initialize Inventory & Stock Transaction
                productDAO.initProductInventory(productId, initialStock, lowStockThreshold, conn);

                conn.commit(); // Commit Transaction
                logger.info("Successfully created product [id={}, sku={}] with initial stock {}", productId, product.getSku(), initialStock);
                
                return getProductById(productId);

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error in product creation transaction", e);
                throw new DatabaseException("Failed to create product due to database error", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error in product creation", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    /**
     * Updates product metadata and refreshes images in a transaction.
     */
    public void updateProduct(Product product, List<ProductImage> images) {
        if (product.getProductId() <= 0) {
            throw new ValidationException("Valid Product ID is required for update.");
        }
        validateProduct(product, product.getProductId());

        if (productDAO.existsBySku(product.getSku(), product.getProductId())) {
            throw new ValidationException("Another product already exists with SKU: " + product.getSku());
        }

        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            product.setSlug(generateSlug(product.getProductName()));
        }
        if (productDAO.existsBySlug(product.getSlug(), product.getProductId())) {
            product.setSlug(product.getSlug() + "-" + product.getProductId());
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                productDAO.updateProduct(product, conn);

                if (images != null && !images.isEmpty()) {
                    productDAO.deleteProductImages(product.getProductId(), conn);
                    for (ProductImage img : images) {
                        img.setProductId(product.getProductId());
                        productDAO.saveProductImage(img, conn);
                    }
                }

                conn.commit();
                logger.info("Successfully updated product ID: {}", product.getProductId());
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error in product update transaction", e);
                throw new DatabaseException("Failed to update product", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error in product update", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    /**
     * Deactivates a product (Soft Delete) to preserve historical order integrity.
     */
    public void deactivateProduct(int productId) {
        productDAO.updateStatus(productId, "INACTIVE");
        logger.info("Product ID {} deactivated (Soft deleted)", productId);
    }

    /**
     * Activates a product.
     */
    public void activateProduct(int productId) {
        productDAO.updateStatus(productId, "ACTIVE");
        logger.info("Product ID {} activated", productId);
    }

    private void validateProduct(Product product, Integer excludeId) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new ValidationException("Product name is required.");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new ValidationException("SKU code is required.");
        }
        if (product.getCategoryId() <= 0) {
            throw new ValidationException("Please select a valid Category.");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Price must be greater than or equal to 0.00.");
        }
        if (product.getDiscountPercentage() != null && 
            (product.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0 || product.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new ValidationException("Discount percentage must be between 0% and 100%.");
        }
    }

    private String generateSlug(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
