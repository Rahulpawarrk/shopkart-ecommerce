package com.example.ecommerce.wishlist.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.wishlist.model.Wishlist;
import com.example.ecommerce.wishlist.service.WishlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller managing customer Wishlist operations.
 * Routes: /wishlist, /wishlist/add, /wishlist/remove, /wishlist/move-to-cart
 * Protected by AuthFilter.
 */
@WebServlet(name = "WishlistServlet", urlPatterns = {
        "/wishlist",
        "/wishlist/add",
        "/wishlist/remove",
        "/wishlist/move-to-cart"
})
public class WishlistServlet extends HttpServlet {

    private WishlistService wishlistService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.wishlistService = new WishlistService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_restricted");
            return;
        }

        String path = request.getServletPath();

        if ("/wishlist/remove".equals(path)) {
            handleRemove(request, response);
        } else {
            showWishlist(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")) 
                          || "true".equalsIgnoreCase(request.getParameter("ajax"));
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"success\":false,\"message\":\"Admin accounts cannot manage customer wishlists.\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_restricted");
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/wishlist/add" -> handleAdd(request, response);
            case "/wishlist/remove" -> handleRemove(request, response);
            case "/wishlist/move-to-cart" -> handleMoveToCart(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/wishlist");
        }
    }

    private void showWishlist(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        Wishlist wishlist = wishlistService.getWishlist(user.getUserId());
        request.setAttribute("wishlist", wishlist);

        request.getRequestDispatcher("/WEB-INF/views/wishlist/wishlist.jsp").forward(request, response);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With")) 
                      || "true".equalsIgnoreCase(request.getParameter("ajax"));
        if (user == null) {
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\":false,\"message\":\"Please sign in to manage your wishlist.\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/wishlist");
            return;
        }
        String returnUrl = request.getParameter("returnUrl");

        try {
            wishlistService.addToWishlist(user.getUserId(), productId);
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":true,\"message\":\"Saved to your Wishlist!\"}");
                return;
            }

            if (ServletUtils.isSafeRedirect(returnUrl)) {
                response.sendRedirect(request.getContextPath() + returnUrl.trim()
                        + (returnUrl.contains("?") ? "&" : "?") + "wishlisted=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/wishlist?added=true");
            }
        } catch (Exception e) {
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/wishlist?error=" + e.getMessage());
        }
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/wishlist");
            return;
        }
        wishlistService.removeFromWishlist(user.getUserId(), productId);

        response.sendRedirect(request.getContextPath() + "/wishlist?removed=true");
    }

    private void handleMoveToCart(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        if (productId <= 0) {
            response.sendRedirect(request.getContextPath() + "/wishlist");
            return;
        }
        int qty = 1;
        String qtyParam = request.getParameter("quantity");
        if (qtyParam != null && !qtyParam.trim().isEmpty()) {
            try {
                qty = Integer.parseInt(qtyParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        try {
            wishlistService.moveToCart(user.getUserId(), productId, qty);
            response.sendRedirect(request.getContextPath() + "/cart?moved=true");
        } catch (ValidationException ve) {
            Wishlist wishlist = wishlistService.getWishlist(user.getUserId());
            request.setAttribute("wishlist", wishlist);
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/wishlist/wishlist.jsp").forward(request, response);
        }
    }
}
