package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.customer.service.AddressService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller managing 3-Step Guided Checkout workflow:
 * 1. /checkout or /checkout/address -> Step 1: Select or Add Delivery Address
 * 2. /checkout/summary -> Step 2: Order Items Summary, Delivery Notes & Apply
 * Coupon
 * 3. /checkout/payment -> Step 3: Select Payment Channel (COD, UPI, Card,
 * NetBanking) & Confirm
 * 
 * Supports both Standard Persistent Cart and Instant Direct "Buy Now".
 * Administrators are strictly blocked from placing customer orders.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = { "/checkout", "/checkout/address", "/checkout/summary",
        "/checkout/payment" })
public class CheckoutServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutServlet.class);

    private CartService cartService;
    private AddressService addressService;
    private OrderService orderService;
    private CouponService couponService;
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.cartService = new CartService();
        this.addressService = new AddressService();
        this.orderService = new OrderService();
        this.couponService = new CouponService();
        this.productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // Enforce RBAC: Admins cannot checkout or place customer orders
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_shop");
            return;
        }

        String path = request.getServletPath();
        String buyNowPid = request.getParameter("buyNowProductId");
        boolean isDirectBuy = buyNowPid != null && !buyNowPid.trim().isEmpty();

        // 1. Prepare Cart Object (Direct Buy vs Standard Cart)
        Cart cart;
        int directProductId = 0;
        int directQuantity = 1;

        if (isDirectBuy) {
            try {
                directProductId = Integer.parseInt(buyNowPid.trim());
                String qtyParam = request.getParameter("quantity");
                if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                    try {
                        directQuantity = Integer.parseInt(qtyParam.trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (directQuantity <= 0)
                    directQuantity = 1;

                Product product = productService.getProductById(directProductId);
                if (!product.isActive() || product.getStockQuantity() < directQuantity) {
                    request.setAttribute("error",
                            "Product '" + product.getProductName() + "' is out of stock or unavailable.");
                }
                cart = Cart.createDirectBuyCart(user.getUserId(), product, directQuantity);
            } catch (Exception e) {
                logger.warn("Failed to load direct buy product", e);
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
        } else {
            cart = cartService.getCart(user.getUserId());
            if (cart.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
        }

        // Apply coupon from session if present
        Coupon sessionCoupon = (Coupon) session.getAttribute("appliedCoupon");
        if (sessionCoupon != null) {
            try {
                couponService.validateAndApplyCoupon(sessionCoupon.getCode(), cart.getSubtotal());
                BigDecimal discount = sessionCoupon.calculateDiscount(cart.getSubtotal());
                cart.setCouponId(sessionCoupon.getCouponId());
                cart.setAppliedCouponCode(sessionCoupon.getCode());
                cart.setCouponDiscount(discount);
            } catch (Exception e) {
                session.removeAttribute("appliedCoupon");
            }
        }

        request.setAttribute("cart", cart);
        request.setAttribute("isDirectBuy", isDirectBuy);
        if (isDirectBuy) {
            request.setAttribute("directBuyProductId", directProductId);
            request.setAttribute("directBuyQuantity", directQuantity);
        }

        List<Address> addresses = addressService.getUserAddresses(user.getUserId());
        request.setAttribute("addresses", addresses);

        // Routing through the 3 distinct checkout steps
        if ("/checkout/summary".equals(path)) {
            handleSummaryStep(request, response, user, addresses, isDirectBuy, directProductId, directQuantity);
        } else if ("/checkout/payment".equals(path)) {
            handlePaymentStep(request, response, user, addresses, isDirectBuy, directProductId, directQuantity);
        } else {
            // Default: Step 1 Address Page (/checkout, /checkout/address)
            handleAddressStep(request, response, addresses);
        }
    }

    private void handleAddressStep(HttpServletRequest request, HttpServletResponse response, List<Address> addresses)
            throws ServletException, IOException {
        String addressIdParam = request.getParameter("addressId");
        if (addressIdParam != null && !addressIdParam.trim().isEmpty()) {
            try {
                request.setAttribute("selectedAddressId", Integer.parseInt(addressIdParam.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/order/checkout-address.jsp").forward(request, response);
    }

    private void handleSummaryStep(HttpServletRequest request, HttpServletResponse response, UserSession user,
            List<Address> addresses, boolean isDirectBuy, int directProductId, int directQuantity)
            throws ServletException, IOException {
        if (addresses == null || addresses.isEmpty()) {
            String addressReturn = "/checkout/address"
                    + (isDirectBuy ? "?buyNowProductId=" + directProductId + "&quantity=" + directQuantity : "");
            response.sendRedirect(request.getContextPath() + addressReturn);
            return;
        }

        Address selectedAddress = resolveSelectedAddress(request, user, addresses);
        if (selectedAddress == null) {
            String addressReturn = "/checkout/address"
                    + (isDirectBuy ? "?buyNowProductId=" + directProductId + "&quantity=" + directQuantity : "");
            response.sendRedirect(request.getContextPath() + addressReturn);
            return;
        }

        request.setAttribute("selectedAddress", selectedAddress);
        request.getRequestDispatcher("/WEB-INF/views/order/checkout-summary.jsp").forward(request, response);
    }

    private void handlePaymentStep(HttpServletRequest request, HttpServletResponse response, UserSession user,
            List<Address> addresses, boolean isDirectBuy, int directProductId, int directQuantity)
            throws ServletException, IOException {
        if (addresses == null || addresses.isEmpty()) {
            String addressReturn = "/checkout/address"
                    + (isDirectBuy ? "?buyNowProductId=" + directProductId + "&quantity=" + directQuantity : "");
            response.sendRedirect(request.getContextPath() + addressReturn);
            return;
        }

        Address selectedAddress = resolveSelectedAddress(request, user, addresses);
        if (selectedAddress == null) {
            String addressReturn = "/checkout/address"
                    + (isDirectBuy ? "?buyNowProductId=" + directProductId + "&quantity=" + directQuantity : "");
            response.sendRedirect(request.getContextPath() + addressReturn);
            return;
        }

        String notes = request.getParameter("notes");
        if (notes != null && !notes.trim().isEmpty()) {
            request.getSession().setAttribute("checkoutNotes", notes.trim());
            request.setAttribute("notes", notes.trim());
        } else {
            String sessionNotes = (String) request.getSession().getAttribute("checkoutNotes");
            request.setAttribute("notes", sessionNotes != null ? sessionNotes : "");
        }

        request.setAttribute("selectedAddress", selectedAddress);
        request.getRequestDispatcher("/WEB-INF/views/order/checkout-payment.jsp").forward(request, response);
    }

    private Address resolveSelectedAddress(HttpServletRequest request, UserSession user, List<Address> addresses) {
        String addressIdParam = request.getParameter("addressId");
        if (addressIdParam != null && !addressIdParam.trim().isEmpty()) {
            try {
                int addressId = Integer.parseInt(addressIdParam.trim());
                for (Address a : addresses) {
                    if (a.getAddressId() == addressId) {
                        return a;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Fallback: Default address or first address
        for (Address a : addresses) {
            if (a.isDefaultAddress()) {
                return a;
            }
        }
        return !addresses.isEmpty() ? addresses.get(0) : null;
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

        // Enforce RBAC: Admins cannot place orders
        if (user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=admin_cannot_shop");
            return;
        }

        String addressIdParam = request.getParameter("addressId");
        String paymentMethod = request.getParameter("paymentMethod");
        String notes = request.getParameter("notes");
        String buyNowPid = request.getParameter("buyNowProductId");
        boolean isDirectBuy = buyNowPid != null && !buyNowPid.trim().isEmpty();

        if (addressIdParam == null || addressIdParam.trim().isEmpty()) {
            request.setAttribute("error", "Please select a shipping delivery address.");
            doGet(request, response);
            return;
        }

        try {
            int addressId = Integer.parseInt(addressIdParam.trim());
            Coupon sessionCoupon = (Coupon) session.getAttribute("appliedCoupon");

            Order confirmedOrder;
            if (isDirectBuy) {
                int productId = Integer.parseInt(buyNowPid.trim());
                int quantity = 1;
                String qtyParam = request.getParameter("quantity");
                if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                    try {
                        quantity = Integer.parseInt(qtyParam.trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (quantity <= 0)
                    quantity = 1;

                confirmedOrder = orderService.processDirectBuyCheckout(user.getUserId(), addressId, productId, quantity,
                        paymentMethod, notes, sessionCoupon);
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("checkoutNotes");
            } else {
                confirmedOrder = orderService.processCheckout(user.getUserId(), addressId, paymentMethod, notes,
                        sessionCoupon);

                // Clean up applied coupon & notes, and update session cart
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("checkoutNotes");
                Cart emptyCart = cartService.getCart(user.getUserId());
                session.setAttribute("cart", emptyCart);
            }

            if ("COD".equalsIgnoreCase(confirmedOrder.getPaymentMethod())) {
                session.setAttribute("justPlacedOrder", confirmedOrder);
                response.sendRedirect(request.getContextPath() + "/home?orderPlaced=true&orderNumber="
                        + confirmedOrder.getOrderNumber());
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/payment/gateway?orderId=" + confirmedOrder.getOrderId());
            }

        } catch (ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            doGet(request, response);
        } catch (Exception e) {
            logger.error("Checkout submission failed", e);
            request.setAttribute("error", "An error occurred while placing your order. Please try again.");
            doGet(request, response);
        }
    }
}