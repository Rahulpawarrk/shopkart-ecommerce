package com.example.ecommerce.coupon.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller handling Promo Code application and removal in customer sessions.
 * Routes: /coupon/apply, /coupon/remove
 */
@WebServlet(name = "CouponServlet", urlPatterns = {"/coupon/apply", "/coupon/remove"})
public class CouponServlet extends HttpServlet {

    private CouponService couponService;
    private CartService cartService;
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.couponService = new CouponService();
        this.cartService = new CartService();
        this.productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/coupon/remove".equals(path)) {
            handleRemove(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/checkout");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/coupon/apply".equals(path)) {
            handleApply(request, response);
        } else if ("/coupon/remove".equals(path)) {
            handleRemove(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/checkout");
        }
    }

    private void handleApply(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        String redirectUrl = resolveRedirectUrl(request, request.getParameter("returnUrl"));

        String code = request.getParameter("couponCode");
        String directBuyProductId = request.getParameter("buyNowProductId");
        String directBuyQuantity = request.getParameter("quantity");

        try {
            BigDecimal subtotal = BigDecimal.ZERO;
            if (directBuyProductId != null && !directBuyProductId.trim().isEmpty()) {
                int pId = Integer.parseInt(directBuyProductId.trim());
                int qty = 1;
                if (directBuyQuantity != null && !directBuyQuantity.trim().isEmpty()) {
                    try {
                        qty = Integer.parseInt(directBuyQuantity.trim());
                    } catch (NumberFormatException ignored) {}
                }
                Product p = productService.getProductById(pId);
                subtotal = p.getEffectivePrice().multiply(BigDecimal.valueOf(qty));
            } else if (session != null && session.getAttribute("currentUser") != null) {
                UserSession user = (UserSession) session.getAttribute("currentUser");
                Cart cart = cartService.getCart(user.getUserId());
                subtotal = cart.getSubtotal();
            }

            Coupon coupon = couponService.validateAndApplyCoupon(code, subtotal);
            if (session != null) {
                session.setAttribute("appliedCoupon", coupon);
            }

            response.sendRedirect(redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "couponApplied=true");
        } catch (ValidationException ve) {
            String errorMsg = URLEncoder.encode(ve.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "couponError=" + errorMsg);
        } catch (Exception e) {
            response.sendRedirect(redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "couponError=Failed+to+apply+coupon");
        }
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        String redirectUrl = resolveRedirectUrl(request, request.getParameter("returnUrl"));

        if (session != null) {
            session.removeAttribute("appliedCoupon");
        }
        response.sendRedirect(redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "couponRemoved=true");
    }

    private String resolveRedirectUrl(HttpServletRequest request, String redirectUrl) {
        if (redirectUrl == null || redirectUrl.trim().isEmpty()) {
            return request.getContextPath() + "/checkout";
        }
        String trimmed = redirectUrl.trim();
        if (trimmed.startsWith(request.getContextPath())) {
            return trimmed;
        }
        return trimmed.startsWith("/") ? request.getContextPath() + trimmed : request.getContextPath() + "/" + trimmed;
    }
}
