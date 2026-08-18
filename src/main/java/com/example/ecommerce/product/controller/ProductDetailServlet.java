package com.example.ecommerce.product.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.review.service.ReviewService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller handling product details view by ID or URL slug with reviews & ratings.
 * Mapped to /product.
 */
@WebServlet(name = "ProductDetailServlet", urlPatterns = {"/product"})
public class ProductDetailServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProductDetailServlet.class);
    private ProductService productService;
    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
        this.reviewService = new ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        String slugParam = request.getParameter("slug");

        try {
            Product product;
            if (idParam != null && !idParam.trim().isEmpty()) {
                int productId = Integer.parseInt(idParam.trim());
                product = productService.getProductById(productId);
            } else if (slugParam != null && !slugParam.trim().isEmpty()) {
                product = productService.getProductBySlug(slugParam.trim());
            } else {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            int reviewPage = 1;
            String pageStr = request.getParameter("reviewPage");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    reviewPage = Math.max(1, Integer.parseInt(pageStr.trim()));
                } catch (NumberFormatException ignored) {}
            }

            // Reviews and Ratings
            RatingSummary ratingSummary = reviewService.getRatingSummary(product.getProductId());
            Pagination<Review> reviews = reviewService.getProductReviews(product.getProductId(), reviewPage, 20);

            // Check verified purchase review eligibility & existing review
            HttpSession session = request.getSession(false);
            boolean canReview = false;
            if (session != null && session.getAttribute("currentUser") != null) {
                UserSession user = (UserSession) session.getAttribute("currentUser");
                java.util.Optional<Review> userRev = reviewService.getUserReviewForProduct(user.getUserId(), product.getProductId());
                if (userRev.isPresent()) {
                    request.setAttribute("existingReview", userRev.get());
                    canReview = true;
                } else {
                    canReview = reviewService.canUserReviewProduct(user.getUserId(), product.getProductId());
                }
                if (canReview) {
                    reviewService.getDeliveredOrderNumber(user.getUserId(), product.getProductId())
                            .ifPresent(ordNum -> request.setAttribute("deliveredOrderNumber", ordNum));
                }
            }

            request.setAttribute("product", product);
            request.setAttribute("ratingSummary", ratingSummary);
            request.setAttribute("reviews", reviews);
            request.setAttribute("canReview", canReview);

            request.getRequestDispatcher("/WEB-INF/views/product/detail.jsp").forward(request, response);

        } catch (ResourceNotFoundException e) {
            logger.warn("Product not found: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error loading product details for idParam: {}, slugParam: {}", idParam, slugParam, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading product details: " + e.getMessage());
        }
    }
}
