package com.example.ecommerce.admin.controller;

import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.model.DiscountType;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for Administrative Coupon & Promotions Management.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminCouponServlet", urlPatterns = {
        "/admin/coupons",
        "/admin/coupons/add",
        "/admin/coupons/new",
        "/admin/coupons/create",
        "/admin/coupons/edit",
        "/admin/coupons/toggle",
        "/admin/coupons/delete"
})
public class AdminCouponServlet extends HttpServlet {

    private CouponService couponService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.couponService = new CouponService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        switch (path) {
            case "/admin/coupons/new", "/admin/coupons/create", "/admin/coupons/add" -> showAddForm(request, response);
            case "/admin/coupons/edit" -> showEditForm(request, response);
            default -> listCoupons(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        switch (path) {
            case "/admin/coupons/new", "/admin/coupons/create", "/admin/coupons/add" -> handleSaveCoupon(request, response, false);
            case "/admin/coupons/edit" -> handleSaveCoupon(request, response, true);
            case "/admin/coupons/toggle" -> handleToggleCoupon(request, response);
            case "/admin/coupons/delete" -> handleDeleteCoupon(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/admin/coupons");
        }
    }

    private void listCoupons(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("q");
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<Coupon> pagination = couponService.getAllCoupons(keyword, page, 15);
        Map<String, Object> stats = couponService.getCouponSummaryStats();

        request.setAttribute("pagination", pagination);
        request.setAttribute("stats", stats);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("isEdit", false);
        request.setAttribute("coupon", new Coupon());
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int couponId = ServletUtils.parseIntParam(request, "id", -1);
        if (couponId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/coupons?error=invalid_id");
            return;
        }
        Coupon coupon = couponService.getCouponById(couponId);
        request.setAttribute("isEdit", true);
        request.setAttribute("coupon", coupon);
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void handleSaveCoupon(HttpServletRequest request, HttpServletResponse response, boolean isEdit) 
            throws ServletException, IOException {
        
        Coupon coupon = new Coupon();
        if (isEdit) {
            int cid = ServletUtils.parseIntParam(request, "couponId", -1);
            if (cid <= 0) {
                response.sendRedirect(request.getContextPath() + "/admin/coupons?error=invalid_id");
                return;
            }
            coupon.setCouponId(cid);
        }

        coupon.setCode(request.getParameter("code"));
        coupon.setDescription(request.getParameter("description"));
        coupon.setDiscountType(DiscountType.valueOf(request.getParameter("discountType")));
        coupon.setDiscountValue(new BigDecimal(request.getParameter("discountValue")));
        
        String minSpend = request.getParameter("minSpend");
        if (minSpend != null && !minSpend.trim().isEmpty()) {
            coupon.setMinSpend(new BigDecimal(minSpend.trim()));
        }

        String maxDiscount = request.getParameter("maxDiscount");
        if (maxDiscount != null && !maxDiscount.trim().isEmpty()) {
            coupon.setMaxDiscount(new BigDecimal(maxDiscount.trim()));
        }

        String startStr = request.getParameter("startDate");
        if (startStr != null && !startStr.trim().isEmpty()) {
            coupon.setStartDate(LocalDateTime.parse(startStr + "T00:00:00"));
        }

        String endStr = request.getParameter("endDate");
        if (endStr != null && !endStr.trim().isEmpty()) {
            coupon.setEndDate(LocalDateTime.parse(endStr + "T23:59:59"));
        }

        String limitStr = request.getParameter("usageLimit");
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            coupon.setUsageLimit(Integer.parseInt(limitStr.trim()));
        }

        coupon.setActive(request.getParameter("active") != null);

        try {
            if (isEdit) {
                couponService.updateCoupon(coupon);
            } else {
                couponService.createCoupon(coupon);
            }
            response.sendRedirect(request.getContextPath() + "/admin/coupons?saved=true");
        } catch (ValidationException ve) {
            request.setAttribute("isEdit", isEdit);
            request.setAttribute("coupon", coupon);
            request.setAttribute("errors", ve.getErrorMessages());
            request.getRequestDispatcher("/index.html").forward(request, response);
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
            request.setAttribute("isEdit", isEdit);
            request.setAttribute("coupon", coupon);
            request.setAttribute("errors", java.util.List.of("Invalid input format: " + e.getMessage()));
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    private void handleToggleCoupon(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int couponId = ServletUtils.parseIntParam(request, "couponId", -1);
        if (couponId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/coupons?error=invalid_id");
            return;
        }
        boolean active = Boolean.parseBoolean(request.getParameter("active"));
        couponService.toggleCouponStatus(couponId, active);
        response.sendRedirect(request.getContextPath() + "/admin/coupons?updated=true");
    }

    private void handleDeleteCoupon(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int couponId = Integer.parseInt(request.getParameter("couponId"));
        couponService.deleteCoupon(couponId);
        response.sendRedirect(request.getContextPath() + "/admin/coupons?deleted=true");
    }
}

