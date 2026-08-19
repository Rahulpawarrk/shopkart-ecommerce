package com.example.ecommerce.category.service;

import com.example.ecommerce.category.dao.CategoryDAO;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Layer handling category hierarchy, tree building, and administrative CRUD.
 */
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * Retrieves all categories as a flat list.
     */
    public List<Category> getAllCategories(boolean activeOnly) {
        return categoryDAO.findAll(activeOnly);
    }

    /**
     * Constructs a hierarchical category tree (Root Categories with populated subcategory lists).
     * Cached in Redis for 15 minutes with instant fallback to DB.
     */
    public List<Category> getCategoryTree(boolean activeOnly) {
        String cacheKey = "shopkart:category:tree:active_" + activeOnly;
        return com.example.ecommerce.common.service.CacheService.getInstance().getOrLoad(
                cacheKey,
                900, // 15 minutes TTL
                new com.fasterxml.jackson.core.type.TypeReference<List<Category>>() {},
                () -> {
                    List<Category> allCategories = categoryDAO.findAll(activeOnly);
                    List<Category> rootCategories = new ArrayList<>();
                    Map<Integer, Category> categoryMap = new HashMap<>();

                    for (Category c : allCategories) {
                        categoryMap.put(c.getCategoryId(), c);
                    }

                    for (Category c : allCategories) {
                        if (c.getParentCategoryId() == null || c.getParentCategoryId() == 0) {
                            rootCategories.add(c);
                        } else {
                            Category parent = categoryMap.get(c.getParentCategoryId());
                            if (parent != null) {
                                parent.addSubcategory(c);
                            } else {
                                rootCategories.add(c); // Fallback if parent missing
                            }
                        }
                    }

                    return rootCategories;
                }
        );
    }

    /**
     * Retrieves single category by ID.
     */
    public Category getCategoryById(int categoryId) {
        return categoryDAO.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    /**
     * Retrieves category by URL slug.
     */
    public Category getCategoryBySlug(String slug) {
        return categoryDAO.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    /**
     * Validates and saves a new category.
     */
    public Category createCategory(Category category) {
        validateCategory(category, null);

        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            category.setSlug(generateSlug(category.getCategoryName()));
        }

        if (categoryDAO.existsBySlug(category.getSlug(), null)) {
            throw new ValidationException("A category with this URL slug already exists.");
        }

        int newId = categoryDAO.createCategory(category);
        category.setCategoryId(newId);
        com.example.ecommerce.common.service.CacheService.getInstance().evictPattern("shopkart:category:*");
        logger.info("Created category: [id={}, name={}]", newId, category.getCategoryName());
        return category;
    }

    /**
     * Validates and updates an existing category.
     */
    public void updateCategory(Category category) {
        if (category.getCategoryId() <= 0) {
            throw new ValidationException("Valid Category ID is required for update.");
        }
        validateCategory(category, category.getCategoryId());

        if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
            category.setSlug(generateSlug(category.getCategoryName()));
        }

        if (categoryDAO.existsBySlug(category.getSlug(), category.getCategoryId())) {
            throw new ValidationException("A category with this URL slug already exists.");
        }

        categoryDAO.updateCategory(category);
        com.example.ecommerce.common.service.CacheService.getInstance().evictPattern("shopkart:category:*");
        logger.info("Updated category ID: {}", category.getCategoryId());
    }

    /**
     * Toggles category active status.
     */
    public void toggleStatus(int categoryId, boolean active) {
        categoryDAO.updateStatus(categoryId, active);
        com.example.ecommerce.common.service.CacheService.getInstance().evictPattern("shopkart:category:*");
        logger.info("Toggled status for category ID: {} to active={}", categoryId, active);
    }

    private void validateCategory(Category category, Integer excludeId) {
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new ValidationException("Category name is required.");
        }
        if (category.getParentCategoryId() != null && category.getParentCategoryId().equals(excludeId)) {
            throw new ValidationException("A category cannot be its own parent.");
        }
    }

    private String generateSlug(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
