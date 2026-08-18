package com.example.ecommerce.category.service;

import com.example.ecommerce.category.dao.CategoryDAO;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests with Mockito")
class CategoryServiceTest {

    @Mock
    private CategoryDAO categoryDAO;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should build hierarchical category tree with parent-child relationships")
    void testGetCategoryTree() {
        Category root1 = new Category(1, null, "Electronics", "electronics", "Gadgets", true);
        Category sub1 = new Category(2, 1, "Laptops", "laptops", "Notebooks", true);
        Category sub2 = new Category(3, 1, "Headphones", "headphones", "Audio", true);
        Category root2 = new Category(4, null, "Fashion", "fashion", "Clothing", true);

        when(categoryDAO.findAll(true)).thenReturn(List.of(root1, sub1, sub2, root2));

        List<Category> tree = categoryService.getCategoryTree(true);

        assertNotNull(tree);
        assertEquals(2, tree.size()); // 2 Root Categories: Electronics and Fashion
        assertEquals("Electronics", tree.get(0).getCategoryName());
        assertEquals(2, tree.get(0).getSubcategories().size());
        assertEquals("Laptops", tree.get(0).getSubcategories().get(0).getCategoryName());
        assertEquals("Headphones", tree.get(0).getSubcategories().get(1).getCategoryName());
    }

    @Test
    @DisplayName("Should reject category creation with empty name")
    void testCreateCategoryEmptyName() {
        Category invalid = new Category();
        invalid.setCategoryName("");

        assertThrows(ValidationException.class, () -> categoryService.createCategory(invalid));
    }

    @Test
    @DisplayName("Should reject duplicate slug on category creation")
    void testCreateCategoryDuplicateSlug() {
        Category cat = new Category();
        cat.setCategoryName("Smartphones");
        cat.setSlug("smartphones");

        when(categoryDAO.existsBySlug("smartphones", null)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> categoryService.createCategory(cat));
        assertTrue(ex.getMessage().contains("slug already exists"));
    }
}
