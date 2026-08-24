package com.example.ecommerce.product.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.review.service.ReviewService;
import com.example.ecommerce.seo.model.SeoMetadata;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller handling product details view by ID or URL slug with reviews & ratings.
 * Mapped to /product and /product/* for clean SEO-friendly URLs.
 */
@WebServlet(name = "ProductDetailServlet", urlPatterns = {"/product", "/product/*"})
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

    public ProductDetailServlet(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    public ProductDetailServlet() {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String idParam = request.getParameter("id");
        String slugParam = request.getParameter("slug");

        try {
            Product product = null;
            boolean shouldRedirectToCanonical = false;

            // 1. Resolve Product by path /product/{slug or id}
            if (pathInfo != null && pathInfo.length() > 1) {
                String segment = pathInfo.substring(1).trim();
                if (segment.matches("\\d+")) {
                    product = productService.getProductById(Integer.parseInt(segment));
                    shouldRedirectToCanonical = true;
                } else {
                    product = productService.getProductBySlug(segment);
                }
            } else if (slugParam != null && !slugParam.trim().isEmpty()) {
                product = productService.getProductBySlug(slugParam.trim());
                shouldRedirectToCanonical = true;
            } else if (idParam != null && !idParam.trim().isEmpty()) {
                String cleanId = idParam.trim();
                if (cleanId.matches("\\d+")) {
                    product = productService.getProductById(Integer.parseInt(cleanId));
                } else {
                    product = productService.getProductBySlug(cleanId);
                }
                shouldRedirectToCanonical = true;
            } else {
                response.sendRedirect(request.getContextPath() + "/products");
                return;
            }

            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found.");
                return;
            }

            // 2. Perform 301 Permanent Redirect to clean SEO URL if requested via legacy query or numeric ID
            String canonicalSlug = (product.getSlug() != null && !product.getSlug().trim().isEmpty()) 
                    ? product.getSlug().trim() 
                    : String.valueOf(product.getProductId());
            String canonicalPath = request.getContextPath() + "/product/" + canonicalSlug;

            if (shouldRedirectToCanonical) {
                String queryString = request.getQueryString();
                // Strip legacy id/slug query params but preserve reviewPage if any
                String targetUrl = canonicalPath;
                if (queryString != null && queryString.contains("reviewPage=")) {
                    targetUrl += "?reviewPage=" + request.getParameter("reviewPage");
                }
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                response.setHeader("Location", targetUrl);
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

            // 3. Build Rich Dynamic SEO Metadata & JSON-LD
            SeoMetadata seo = buildProductSeoMetadata(product, ratingSummary, reviews, canonicalSlug);
            request.setAttribute("seo", seo);

            request.setAttribute("product", product);
            request.setAttribute("ratingSummary", ratingSummary);
            request.setAttribute("reviews", reviews);
            request.setAttribute("canReview", canReview);

            request.getRequestDispatcher("/WEB-INF/views/product/detail.jsp").forward(request, response);

        } catch (ResourceNotFoundException e) {
            logger.warn("Product not found: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error loading product details for pathInfo: {}, idParam: {}", pathInfo, idParam, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading product details: " + e.getMessage());
        }
    }

    private SeoMetadata buildProductSeoMetadata(Product product, RatingSummary ratingSummary, Pagination<Review> reviews, String slug) {
        SeoMetadata seo = new SeoMetadata();
        
        String brandName = product.getBrand() != null && !product.getBrand().trim().isEmpty() ? product.getBrand().trim() : "ShopKart";
        String priceStr = product.getDiscountedPrice() != null ? product.getDiscountedPrice().toPlainString() : (product.getPrice() != null ? product.getPrice().toPlainString() : "0.00");
        
        seo.setTitle(product.getProductName() + " - Buy Online at \u20B9" + priceStr + " | ShopKart");
        
        String desc = product.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = "Buy " + product.getProductName() + " by " + brandName + " online at best price in India on ShopKart. Guaranteed genuine product with fast shipping.";
        } else {
            desc = desc.replaceAll("\\s+", " ").trim();
            if (desc.length() > 160) {
                desc = desc.substring(0, 157) + "...";
            }
        }
        seo.setDescription(desc);
        
        String fullCanonical = SeoMetadata.BASE_URL + "/product/" + slug;
        seo.setCanonicalUrl(fullCanonical);
        seo.setRobots("index, follow");
        seo.setOgType("product");
        seo.setOgTitle(product.getProductName() + " | ShopKart");
        seo.setOgDescription(desc);
        
        if (product.getPrimaryImageUrl() != null && !product.getPrimaryImageUrl().trim().isEmpty()) {
            seo.setOgImage(product.getPrimaryImageUrl().trim());
        }
        
        seo.setProductPriceAmount(priceStr);
        seo.setProductPriceCurrency("INR");
        seo.setProductAvailability(product.getStockQuantity() > 0 ? "https://schema.org/InStock" : "https://schema.org/OutOfStock");

        // Breadcrumbs
        seo.addBreadcrumb("Home", SeoMetadata.BASE_URL + "/");
        if (product.getCategory() != null) {
            String catSlug = product.getCategory().getSlug() != null ? product.getCategory().getSlug() : String.valueOf(product.getCategoryId());
            seo.addBreadcrumb(product.getCategory().getCategoryName(), SeoMetadata.BASE_URL + "/category/" + catSlug);
        }
        seo.addBreadcrumb(product.getProductName(), fullCanonical);

        return seo;
    }
}
