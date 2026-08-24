package com.example.ecommerce.admin.controller;

import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.review.service.ReviewService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for Administrative Review Moderation.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminReviewServlet", urlPatterns = {
        "/admin/reviews",
        "/admin/reviews/approve",
        "/admin/reviews/delete"
})
public class AdminReviewServlet extends HttpServlet {

    private ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.reviewService = new ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("q");
        String statusParam = request.getParameter("status"); // "ALL", "APPROVED", "PENDING"
        Boolean isApproved = null;
        if ("APPROVED".equalsIgnoreCase(statusParam))
            isApproved = true;
        if ("PENDING".equalsIgnoreCase(statusParam))
            isApproved = false;

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Pagination<Review> pagination = reviewService.getAllReviews(keyword, isApproved, page, 15);

        request.setAttribute("pagination", pagination);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", statusParam != null ? statusParam : "ALL");

        request.getRequestDispatcher("/WEB-INF/views/admin/review-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/admin/reviews/approve".equals(path)) {
            int reviewId = ServletUtils.parseIntParam(request, "reviewId", -1);
            if (reviewId <= 0) return;
            boolean approved = Boolean.parseBoolean(request.getParameter("approved"));
            reviewService.setReviewApproval(reviewId, approved);
            response.sendRedirect(request.getContextPath() + "/admin/reviews?updated=true");
        } else if ("/admin/reviews/delete".equals(path)) {
            int reviewId = ServletUtils.parseIntParam(request, "reviewId", -1);
            if (reviewId <= 0) return;
            reviewService.deleteReview(reviewId);
            response.sendRedirect(request.getContextPath() + "/admin/reviews?deleted=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/reviews");
        }
    }
}
