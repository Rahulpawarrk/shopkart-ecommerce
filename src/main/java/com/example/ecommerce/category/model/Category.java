package com.example.ecommerce.category.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Entity supporting hierarchical trees (Parent -> Subcategories).
 */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private int categoryId;
    private Integer parentCategoryId;
    private String categoryName;
    private String slug;
    private String description;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient hierarchical fields
    private String parentCategoryName;
    private List<Category> subcategories = new ArrayList<>();

    public Category() {}

    public Category(int categoryId, Integer parentCategoryId, String categoryName, String slug, String description, boolean active) {
        this.categoryId = categoryId;
        this.parentCategoryId = parentCategoryId;
        this.categoryName = categoryName;
        this.slug = slug;
        this.description = description;
        this.active = active;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public List<Category> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<Category> subcategories) {
        this.subcategories = subcategories;
    }

    public void addSubcategory(Category subcategory) {
        if (this.subcategories == null) {
            this.subcategories = new ArrayList<>();
        }
        this.subcategories.add(subcategory);
    }
}
