package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.CreateReviewRequest;
import com.example.ecommerce.api.dto.ReviewDto;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.review.service.ReviewService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for Product Customer Reviews & Rating breakdowns.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewRestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductReviews(
            @PathVariable int productId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize
    ) {
        RatingSummary ratingSummary = reviewService.getRatingSummary(productId);
        Pagination<Review> reviews = reviewService.getProductReviews(productId, page, pageSize);

        var reviewDtos = reviews.getItems().stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "summary", ratingSummary,
                "reviews", reviewDtos,
                "totalReviews", reviews.getTotalItems(),
                "page", reviews.getCurrentPage(),
                "totalPages", reviews.getTotalPages()
        )));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @Valid @RequestBody CreateReviewRequest req,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to submit a review", "UNAUTHORIZED"));
        }

        java.util.List<String> images = req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()
                ? java.util.List.of(req.getImageUrl().trim()) : null;

        Review created = reviewService.submitReview(
                user.getUserId(),
                req.getProductId(),
                req.getRating(),
                req.getTitle().trim(),
                req.getComment().trim(),
                images
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thank you! Your review has been submitted.", ReviewDto.fromEntity(created)));
    }
}
