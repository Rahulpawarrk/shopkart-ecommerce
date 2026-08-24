package com.example.ecommerce.review.service;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.review.dao.ReviewDAO;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer enforcing Verified Purchase validation, rating bounds, and review moderation.
 */
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewDAO reviewDAO;
    private final ProductDAO productDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.productDAO = new ProductDAO();
    }

    public ReviewService(ReviewDAO reviewDAO, ProductDAO productDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
    }

    /**
     * Submits or updates a product review with Verified Purchase enforcement and image attachments.
     */
    public Review submitReview(int userId, int productId, int rating, String title, String comment) {
        return submitReview(userId, productId, rating, title, comment, null);
    }

    public Review submitReview(int userId, int productId, int rating, String title, String comment, List<String> imageUrls) {
        // 1. Validate Input
        List<String> errors = new ArrayList<>();
        if (rating < 1 || rating > 5) {
            errors.add("Rating score must be between 1 and 5 stars.");
        }
        if (title == null || title.trim().isEmpty()) {
            errors.add("Review headline / title is required.");
        } else if (title.trim().length() < 3 || title.trim().length() > 150) {
            errors.add("Review headline must be between 3 and 150 characters.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            errors.add("Review text / feedback is required.");
        } else if (comment.trim().length() < 10 || comment.trim().length() > 2000) {
            errors.add("Review feedback must be between 10 and 2000 characters.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // 2. Verify Product Exists
        productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // 3. Enforce Verified Purchase Check (Must be a delivered order)
        boolean isVerified = reviewDAO.isVerifiedPurchase(userId, productId);
        if (!isVerified) {
            logger.warn("Review rejected: User {} attempted to review unpurchased/undelivered product {}", userId, productId);
            throw new ValidationException("Only customers with verified delivered orders can write a product review.");
        }

        String primaryImage = (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;

        // 4. Create or Update Review
        Optional<Review> existing = reviewDAO.findByUserAndProduct(userId, productId);
        if (existing.isPresent()) {
            Review rev = existing.get();
            rev.setRating(rating);
            rev.setTitle(title.trim());
            rev.setComment(comment.trim());
            if (primaryImage != null) {
                rev.setImageUrl(primaryImage);
                rev.setImageUrls(imageUrls);
            }
            reviewDAO.updateReview(rev);
            logger.info("Updated existing review [id={}] for product [{}] by user [{}] with {} image(s)", 
                    rev.getReviewId(), productId, userId, imageUrls != null ? imageUrls.size() : 0);
            return rev;
        } else {
            Review rev = new Review(productId, userId, rating, title.trim(), comment.trim(), primaryImage, true);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                rev.setImageUrls(imageUrls);
            }
            int reviewId = reviewDAO.createReview(rev);
            rev.setReviewId(reviewId);
            logger.info("Created new verified review [id={}] for product [{}] by user [{}] with {} image(s)", 
                    reviewId, productId, userId, imageUrls != null ? imageUrls.size() : 0);
            return rev;
        }
    }

    public Optional<String> getDeliveredOrderNumber(int userId, int productId) {
        return reviewDAO.getDeliveredOrderNumber(userId, productId);
    }

    public Pagination<Review> getProductReviews(int productId, int page, int pageSize) {
        return reviewDAO.findByProductId(productId, true, page, pageSize);
    }

    public RatingSummary getRatingSummary(int productId) {
        return reviewDAO.getRatingSummary(productId);
    }

    public boolean canUserReviewProduct(int userId, int productId) {
        return reviewDAO.isVerifiedPurchase(userId, productId);
    }

    public Optional<Review> getUserReviewForProduct(int userId, int productId) {
        return reviewDAO.findByUserAndProduct(userId, productId);
    }

    // Admin Moderation
    public Pagination<Review> getAllReviews(String keyword, Boolean isApproved, int page, int pageSize) {
        return reviewDAO.findAll(keyword, isApproved, page, pageSize);
    }

    public void setReviewApproval(int reviewId, boolean isApproved) {
        reviewDAO.setApprovalStatus(reviewId, isApproved);
        logger.info("Admin updated review [id={}] approval status to: {}", reviewId, isApproved);
    }

    public void deleteReview(int reviewId) {
        reviewDAO.deleteReview(reviewId);
        logger.info("Admin deleted review [id={}]", reviewId);
    }
}
