package com.example.ecommerce.api.dto;

import com.example.ecommerce.category.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryDto {
    private int categoryId;
    private Integer parentCategoryId;
    private String categoryName;
    private String slug;
    private String description;
    private boolean active;
    private List<CategoryDto> subCategories = new ArrayList<>();

    public CategoryDto() {}

    public static CategoryDto fromEntity(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setParentCategoryId(category.getParentCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setActive(category.isActive());
        if (category.getSubcategories() != null) {
            for (Category sub : category.getSubcategories()) {
                dto.getSubCategories().add(CategoryDto.fromEntity(sub));
            }
        }
        return dto;
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

    public List<CategoryDto> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryDto> subCategories) {
        this.subCategories = subCategories;
    }
}
