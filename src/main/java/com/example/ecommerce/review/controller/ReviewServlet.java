package com.example.ecommerce.review.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.review.service.ReviewService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Controller handling customer Review submissions with Image Upload on Delivered Orders and Product Detail pages.
 * Routes: /product/review, /order/review
 * Protected by AuthFilter.
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/product/review", "/order/review"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB memory buffer
    maxFileSize = 1024 * 1024 * 10,       // 10 MB per image
    maxRequestSize = 1024 * 1024 * 30     // 30 MB total request
)
public class ReviewServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);
    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.reviewService = new ReviewService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String productIdStr = request.getParameter("productId");
        String ratingStr = request.getParameter("rating");
        String title = request.getParameter("title");
        String comment = request.getParameter("comment");
        String orderIdStr = request.getParameter("orderId");

        int productId;
        int rating;
        try {
            productId = Integer.parseInt(productIdStr);
            rating = Integer.parseInt(ratingStr);
        } catch (NumberFormatException | NullPointerException e) {
            handleErrorRedirect(request, response, orderIdStr, productIdStr, "Invalid product or rating parameter.");
            return;
        }

        // Process review images
        List<String> imageUrls = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                if ("reviewImage".equals(part.getName()) || "reviewImages".equals(part.getName())) {
                    String submittedFileName = part.getSubmittedFileName();
                    if (submittedFileName != null && !submittedFileName.trim().isEmpty() && part.getSize() > 0) {
                        String imageUrl = saveUploadedReviewImage(request, part, user.getUserId(), productId);
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Could not parse multipart image parts, proceeding with text review: {}", ex.getMessage());
        }

        try {
            reviewService.submitReview(user.getUserId(), productId, rating, title, comment, imageUrls);
            handleSuccessRedirect(request, response, orderIdStr, productId);
        } catch (ValidationException ve) {
            handleErrorRedirect(request, response, orderIdStr, String.valueOf(productId), ve.getMessage());
        } catch (Exception e) {
            logger.error("Error submitting review for product [{}]: {}", productId, e.getMessage(), e);
            handleErrorRedirect(request, response, orderIdStr, String.valueOf(productId), "Failed to submit review. Please try again.");
        }
    }

    private String saveUploadedReviewImage(HttpServletRequest request, Part part, int userId, int productId) {
        try {
            String submittedFileName = part.getSubmittedFileName();
            String extension = ".jpg";
            int dotIdx = submittedFileName.lastIndexOf('.');
            if (dotIdx > 0) {
                String ext = submittedFileName.substring(dotIdx).toLowerCase();
                if (ext.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                    extension = ext;
                }
            }

            String fileName = "rev_u" + userId + "_p" + productId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            
            // Try saving to webapp uploads directory
            String realPath = request.getServletContext().getRealPath("/uploads/reviews");
            if (realPath != null) {
                File uploadDir = new File(realPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                File targetFile = new File(uploadDir, fileName);
                try (InputStream in = part.getInputStream()) {
                    Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                return request.getContextPath() + "/uploads/reviews/" + fileName;
            }

            // Fallback to Data URI Base64 if realPath is unavailable (e.g. unexploded WAR)
            try (InputStream in = part.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                String mime = part.getContentType() != null ? part.getContentType() : "image/jpeg";
                return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            logger.error("Failed to save uploaded review image", e);
            return null;
        }
    }

    private void handleSuccessRedirect(HttpServletRequest request, HttpServletResponse response, String orderIdStr, int productId) throws IOException {
        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/order?id=" + orderIdStr.trim() + "&reviewSubmitted=true&reviewedProductId=" + productId);
        } else {
            response.sendRedirect(request.getContextPath() + "/product?id=" + productId + "&reviewSubmitted=true#reviews");
        }
    }

    private void handleErrorRedirect(HttpServletRequest request, HttpServletResponse response, String orderIdStr, String productIdStr, String errorMsg) throws IOException {
        String encoded = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/order?id=" + orderIdStr.trim() + "&reviewError=" + encoded);
        } else {
            response.sendRedirect(request.getContextPath() + "/product?id=" + (productIdStr != null ? productIdStr : "1") + "&reviewError=" + encoded + "#reviews");
        }
    }
}
