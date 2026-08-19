package com.example.ecommerce.cart.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Controller managing customer Shopping Cart operations and Promotional
 * Coupons.
 * Routes: /cart, /cart/add, /cart/update, /cart/remove, /cart/clear,
 * /cart/coupon, /cart/coupon/remove
 * Administrators are strictly restricted from adding to cart or purchasing.
 */
@WebServlet(name = "CartServlet", urlPatterns = {
        "/cart",
        "/cart/add",
        "/cart/update",
        "/cart/remove",
        "/cart/clear",
        "/cart/coupon",
        "/cart/coupon/remove"
})
public class CartServlet extends HttpServlet {

    private CartService cartService;
    private CouponService couponService;
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.cartService = new CartService();
        this.couponService = new CouponService();
        this.productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        // Block Admin from viewing/shopping via customer cart
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_shop");
            return;
        }

        String path = request.getServletPath();

        if ("/cart/remove".equals(path)) {
            handleRemove(request, response);
        } else if ("/cart/coupon/remove".equals(path)) {
            handleRemoveCoupon(request, response);
        } else if ("/cart/add".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/cart");
        } else {
            showCart(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        // Block Admin from adding/mutating items in cart
        if (user != null && user.isAdmin()) {
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                    || "true".equalsIgnoreCase(request.getParameter("ajax"));
            if (isAjax) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Admin accounts cannot add items to cart or place orders.\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_shop");
            return;
        }

        String path = request.getServletPath();

        switch (path) {
            case "/cart/add" -> handleAdd(request, response);
            case "/cart/update" -> handleUpdate(request, response);
            case "/cart/remove" -> handleRemove(request, response);
            case "/cart/clear" -> handleClear(request, response);
            case "/cart/coupon" -> handleApplyCoupon(request, response);
            case "/cart/coupon/remove" -> handleRemoveCoupon(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    private void showCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        Cart cart = cartService.getCart(user.getUserId());
        if (session != null) {
            session.setAttribute("cart", cart);
        }

        // Fetch suggested active products for the Quick Add More Products modal and
        // carousel
        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria();
            criteria.setStatus("ACTIVE");
            criteria.setPageSize(24);
            Pagination<Product> catalogPage = productService.searchCatalog(criteria);
            request.setAttribute("suggestedProducts", catalogPage.getItems());
        } catch (Exception ignored) {
        }

        request.setAttribute("cart", cart);
        request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String[] productIds = request.getParameterValues("productId");
        String[] quantities = request.getParameterValues("quantity");

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "true".equalsIgnoreCase(request.getParameter("ajax"));

        if (productIds == null || productIds.length == 0) {
            String singleId = request.getParameter("productId");
            if (singleId != null && !singleId.trim().isEmpty()) {
                productIds = new String[] { singleId.trim() };
            }
        }

        if (productIds == null || productIds.length == 0) {
            if (isAjax) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"No products specified\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        int addedCount = 0;
        String lastError = null;

        for (int i = 0; i < productIds.length; i++) {
            String pidStr = productIds[i];
            if (pidStr == null || pidStr.trim().isEmpty())
                continue;
            try {
                int productId = Integer.parseInt(pidStr.trim());
                int quantity = 1;
                if (quantities != null && i < quantities.length && quantities[i] != null
                        && !quantities[i].trim().isEmpty()) {
                    try {
                        quantity = Integer.parseInt(quantities[i].trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                cartService.addToCart(user.getUserId(), productId, quantity);
                addedCount++;
            } catch (ValidationException ve) {
                lastError = ve.getMessage();
            } catch (NumberFormatException ignored) {
            }
        }

        Cart cart = cartService.getCart(user.getUserId());
        if (session != null) {
            session.setAttribute("cart", cart);
        }

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if (addedCount > 0) {
                String msg = addedCount > 1 ? (addedCount + " items added to cart!") : "Item added to cart!";
                response.getWriter().write("{\"success\":true,\"message\":\"" + msg + "\",\"totalItems\":"
                        + cart.getTotalQuantity() + ",\"addedCount\":" + addedCount + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\""
                        + (lastError != null ? lastError.replace("\"", "\\\"") : "Failed to add products to cart")
                        + "\"}");
            }
            return;
        }

        String returnUrl = request.getParameter("returnUrl");
        if (returnUrl != null && !returnUrl.trim().isEmpty() && !returnUrl.contains("\n")
                && !returnUrl.contains("\r")) {
            response.sendRedirect(returnUrl + (returnUrl.contains("?") ? "&" : "?") + "added=true");
        } else {
            response.sendRedirect(request.getContextPath() + "/cart?added=true");
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        int productId = Integer.parseInt(request.getParameter("productId"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        try {
            cartService.updateQuantity(user.getUserId(), productId, quantity);
            Cart cart = cartService.getCart(user.getUserId());
            if (session != null) {
                session.setAttribute("cart", cart);
            }
            response.sendRedirect(request.getContextPath() + "/cart?updated=true");
        } catch (ValidationException ve) {
            Cart cart = cartService.getCart(user.getUserId());
            if (session != null) {
                session.setAttribute("cart", cart);
            }
            request.setAttribute("cart", cart);
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
        }
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        int productId = Integer.parseInt(request.getParameter("productId"));
        cartService.removeFromCart(user.getUserId(), productId);
        Cart cart = cartService.getCart(user.getUserId());
        if (session != null) {
            session.setAttribute("cart", cart);
        }
        response.sendRedirect(request.getContextPath() + "/cart?removed=true");
    }

    private void handleClear(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        cartService.clearCart(user.getUserId());
        if (session != null) {
            session.setAttribute("cart", new Cart());
            session.removeAttribute("appliedCoupon");
        }
        response.sendRedirect(request.getContextPath() + "/cart?cleared=true");
    }

    private void handleApplyCoupon(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String couponCode = request.getParameter("couponCode");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "true".equalsIgnoreCase(request.getParameter("ajax"));

        Cart cart = cartService.getCart(user.getUserId());
        if (cart.isEmpty()) {
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Your cart is empty.\"}");
                return;
            }
            request.setAttribute("cart", cart);
            request.setAttribute("error", "Your cart is empty.");
            request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
            return;
        }

        try {
            Coupon coupon = couponService.validateAndApplyCoupon(couponCode, cart.getSubtotal());
            session.setAttribute("appliedCoupon", coupon);

            BigDecimal discount = coupon.calculateDiscount(cart.getSubtotal());
            cart.setCouponId(coupon.getCouponId());
            cart.setAppliedCouponCode(coupon.getCode());
            cart.setCouponDiscount(discount);

            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter()
                        .write("{\"success\":true,\"message\":\"Coupon '" + coupon.getCode() + "' applied! You saved ₹"
                                + discount.setScale(0, RoundingMode.HALF_UP) + "\",\"discount\":" + discount
                                + ",\"grandTotal\":" + cart.getGrandTotal() + ",\"couponCode\":\"" + coupon.getCode()
                                + "\"}");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/cart?couponApplied=true");

        } catch (ValidationException | ResourceNotFoundException ve) {
            session.removeAttribute("appliedCoupon");
            if (isAjax) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter()
                        .write("{\"success\":false,\"message\":\"" + ve.getMessage().replace("\"", "\\\"") + "\"}");
                return;
            }
            request.setAttribute("cart", cart);
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(request, response);
        }
    }

    private void handleRemoveCoupon(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("appliedCoupon");
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "true".equalsIgnoreCase(request.getParameter("ajax"));

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":true,\"message\":\"Coupon removed successfully.\"}");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart?couponRemoved=true");
    }
}