package com.example.ecommerce.review.service;

import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.review.dao.ReviewDAO;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests with Mockito")
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    @Mock
    private ProductDAO productDAO;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("Should successfully submit review when user has a verified delivered purchase")
    void testSubmitReviewVerifiedPurchaseSuccess() {
        Product p = new Product();
        p.setProductId(1);
        p.setProductName("Gaming Laptop");

        when(productDAO.findById(1)).thenReturn(Optional.of(p));
        when(reviewDAO.isVerifiedPurchase(10, 1)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(10, 1)).thenReturn(Optional.empty());
        when(reviewDAO.createReview(any(Review.class))).thenReturn(1001);

        Review review = reviewService.submitReview(10, 1, 5, "Awesome Laptop", "Super fast and runs quiet.");

        assertNotNull(review);
        assertEquals(1001, review.getReviewId());
        assertEquals(5, review.getRating());
        assertTrue(review.isVerifiedPurchase());
    }

    @Test
    @DisplayName("Should reject review submission when user has not purchased/received the product")
    void testSubmitReviewNonVerifiedPurchaseThrowsException() {
        Product p = new Product();
        p.setProductId(2);

        when(productDAO.findById(2)).thenReturn(Optional.of(p));
        when(reviewDAO.isVerifiedPurchase(10, 2)).thenReturn(false); // Unverified

        ValidationException ex = assertThrows(ValidationException.class, () ->
            reviewService.submitReview(10, 2, 5, "Great product", "Love it, really good!")
        );

        assertTrue(ex.getMessage().contains("verified delivered orders"));
        verify(reviewDAO, never()).createReview(any());
    }

    @Test
    @DisplayName("Should reject ratings outside 1-5 bounds")
    void testSubmitReviewInvalidRatingBounds() {
        assertThrows(ValidationException.class, () ->
            reviewService.submitReview(10, 1, 6, "Title here", "Valid review comment text exceeding 10 chars")
        );
        assertThrows(ValidationException.class, () ->
            reviewService.submitReview(10, 1, 0, "Title here", "Valid review comment text exceeding 10 chars")
        );
    }

    @Test
    @DisplayName("Should correctly calculate rating percentages in RatingSummary")
    void testRatingSummaryPercentages() {
        RatingSummary summary = new RatingSummary(1, 4.5, 10);
        summary.setFiveStarCount(6); // 60%
        summary.setFourStarCount(3); // 30%
        summary.setThreeStarCount(1); // 10%
        summary.setTwoStarCount(0);
        summary.setOneStarCount(0);

        assertEquals(60, summary.getFiveStarPercentage());
        assertEquals(30, summary.getFourStarPercentage());
        assertEquals(10, summary.getThreeStarPercentage());
        assertEquals(0, summary.getTwoStarPercentage());
        assertEquals(0, summary.getOneStarPercentage());
    }

    @Test
    @DisplayName("Should successfully update existing review when user has already reviewed the product")
    void testUpdateExistingReviewWhenAlreadyReviewed() {
        Product p = new Product();
        p.setProductId(1);
        p.setProductName("Gaming Laptop");

        Review existingRev = new Review(1, 10, 4, "Old Title", "Old Comment", null, true);
        existingRev.setReviewId(555);

        when(productDAO.findById(1)).thenReturn(Optional.of(p));
        when(reviewDAO.isVerifiedPurchase(10, 1)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(10, 1)).thenReturn(Optional.of(existingRev));

        Review updated = reviewService.submitReview(10, 1, 5, "Updated Title", "Updated Comment after 1 month of use");

        assertNotNull(updated);
        assertEquals(555, updated.getReviewId());
        assertEquals(5, updated.getRating());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Comment after 1 month of use", updated.getComment());

        verify(reviewDAO, times(1)).updateReview(existingRev);
        verify(reviewDAO, never()).createReview(any());
    }

    @Test
    @DisplayName("Should successfully submit review with uploaded product photos")
    void testSubmitReviewWithImagesSuccess() {
        Product p = new Product();
        p.setProductId(1);
        p.setProductName("Gaming Laptop");

        when(productDAO.findById(1)).thenReturn(Optional.of(p));
        when(reviewDAO.isVerifiedPurchase(10, 1)).thenReturn(true);
        when(reviewDAO.findByUserAndProduct(10, 1)).thenReturn(Optional.empty());
        when(reviewDAO.createReview(any(Review.class))).thenReturn(1002);

        java.util.List<String> images = java.util.Arrays.asList("/uploads/reviews/rev_1.jpg", "/uploads/reviews/rev_2.jpg");
        Review review = reviewService.submitReview(10, 1, 5, "Unboxing photo attached", "Looks amazing in person!", images);

        assertNotNull(review);
        assertEquals(1002, review.getReviewId());
        assertEquals("/uploads/reviews/rev_1.jpg", review.getImageUrl());
        assertTrue(review.hasImages());
        assertEquals(2, review.getImageUrls().size());
    }

    @Test
    @DisplayName("Should test RatingSummary and Review formatting helpers")
    void testRatingSummaryAndReviewHelpers() {
        RatingSummary summary = new RatingSummary(1, 4.8, 15);
        assertEquals("★★★★★", summary.getStarsDisplay());
        assertEquals("4.8", summary.getFormattedAverage());

        Review review = new Review();
        review.setRating(4);
        assertEquals("★★★★☆", review.getStarsDisplay());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        review.setCreatedAt(now.minusDays(5));
        review.setUpdatedAt(now);
        assertTrue(review.isEdited());
        assertNotNull(review.getFormattedCreatedAt());
        assertNotNull(review.getFormattedUpdatedAt());
    }
}
