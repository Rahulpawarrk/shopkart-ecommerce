package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.CategoryDto;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for category navigation and hierarchy.
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategoryTree() {
        List<Category> tree = categoryService.getCategoryTree(true);
        List<CategoryDto> dtos = tree.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<Category> all = categoryService.getAllCategories(true);
        List<CategoryDto> dtos = all.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/{slugOrId}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategory(@PathVariable String slugOrId) {
        Category category = null;
        if (slugOrId.matches("\\d+")) {
            category = categoryService.getCategoryById(Integer.parseInt(slugOrId));
        }
        if (category == null) {
            category = categoryService.getCategoryBySlug(slugOrId);
        }

        if (category == null) {
            throw new ResourceNotFoundException("Category not found: " + slugOrId);
        }

        return ResponseEntity.ok(ApiResponse.ok(CategoryDto.fromEntity(category)));
    }
}
