package com.example.ecommerce.category.repository;

import com.example.ecommerce.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByActiveTrue();
    List<Category> findByParentCategoryIdIsNullAndActiveTrue();
    List<Category> findByParentCategoryIdAndActiveTrue(int parentCategoryId);
}
