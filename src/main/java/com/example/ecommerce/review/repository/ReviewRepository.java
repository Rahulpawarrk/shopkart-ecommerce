package com.example.ecommerce.review.repository;

import com.example.ecommerce.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductId(int productId);
    Page<Review> findByProductId(int productId, Pageable pageable);
    Optional<Review> findByProductIdAndUserId(int productId, int userId);
}
