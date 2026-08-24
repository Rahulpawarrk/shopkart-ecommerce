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
import com.example.ecommerce.order.model.OrderConfirmationContext;
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
 * 2. /checkout/summary -> Step 2: Order Items Summary, Delivery Notes & Apply Coupon
 * 3. /checkout/payment -> Step 3: Select Payment Channel (UPI, Cards, NetBanking, COD) & Confirm
 * 
 * Supports 100% clean address bar URLs via session-bound state for both standard Cart and instant Buy Now.
 * Automatically cleans query parameters from browser address bar upon arrival.
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

        // 1. If Buy Now query params are present in URL, store into session and REDIRECT immediately to clean the address bar!
        String buyNowPidParam = request.getParameter("buyNowProductId");
        if (buyNowPidParam != null && !buyNowPidParam.trim().isEmpty()) {
            try {
                int pid = Integer.parseInt(buyNowPidParam.trim());
                int qty = 1;
                String qtyParam = request.getParameter("quantity");
                if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                    try {
                        qty = Integer.parseInt(qtyParam.trim());
                    } catch (NumberFormatException ignored) {}
                }
                if (qty <= 0) qty = 1;

                session.setAttribute("directBuyProductId", pid);
                session.setAttribute("directBuyQuantity", qty);

                // Clean address bar immediately!
                response.sendRedirect(request.getContextPath() + "/checkout");
                return;
            } catch (NumberFormatException ignored) {}
        }

        Integer sessionBuyNowPid = (session != null) ? (Integer) session.getAttribute("directBuyProductId") : null;
        boolean isDirectBuy = (sessionBuyNowPid != null && sessionBuyNowPid > 0);
        int directProductId = isDirectBuy ? sessionBuyNowPid : 0;
        int directQuantity = 1;
        if (isDirectBuy && session.getAttribute("directBuyQuantity") != null) {
            directQuantity = (Integer) session.getAttribute("directBuyQuantity");
        }

        // 2. Prepare Cart Object (Direct Buy vs Standard Cart)
        Cart cart;
        if (isDirectBuy && directProductId > 0) {
            try {
                Product product = productService.getProductById(directProductId);
                if (!product.isActive() || product.getStockQuantity() < directQuantity) {
                    request.setAttribute("error",
                            "Product '" + product.getProductName() + "' is out of stock or unavailable.");
                }
                cart = Cart.createDirectBuyCart(user.getUserId(), product, directQuantity);
            } catch (Exception e) {
                logger.warn("Failed to load direct buy product", e);
                if (session != null) {
                    session.removeAttribute("directBuyProductId");
                    session.removeAttribute("directBuyQuantity");
                }
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

        // Store selected address in session if present in request and clean URL if needed
        String addressIdParam = request.getParameter("addressId");
        if (addressIdParam != null && !addressIdParam.trim().isEmpty()) {
            try {
                int aid = Integer.parseInt(addressIdParam.trim());
                if (session != null) session.setAttribute("checkoutAddressId", aid);
            } catch (NumberFormatException ignored) {}
        }

        // Routing through the 3 distinct checkout steps
        if ("/checkout/summary".equals(path)) {
            handleSummaryStep(request, response, user, addresses);
        } else if ("/checkout/payment".equals(path)) {
            handlePaymentStep(request, response, user, addresses);
        } else {
            // Default: Step 1 Address Page (/checkout, /checkout/address)
            handleAddressStep(request, response, addresses);
        }
    }

    private void handleAddressStep(HttpServletRequest request, HttpServletResponse response, List<Address> addresses)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer sessionAddressId = (session != null) ? (Integer) session.getAttribute("checkoutAddressId") : null;
        if (sessionAddressId != null) {
            request.setAttribute("selectedAddressId", sessionAddressId);
        }
        request.getRequestDispatcher("/WEB-INF/views/order/checkout-address.jsp").forward(request, response);
    }

    private void handleSummaryStep(HttpServletRequest request, HttpServletResponse response, UserSession user,
            List<Address> addresses)
            throws ServletException, IOException {
        if (addresses == null || addresses.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/checkout/address");
            return;
        }

        Address selectedAddress = resolveSelectedAddress(request, user, addresses);
        if (selectedAddress == null) {
            response.sendRedirect(request.getContextPath() + "/checkout/address");
            return;
        }

        request.setAttribute("selectedAddress", selectedAddress);
        request.getRequestDispatcher("/WEB-INF/views/order/checkout-summary.jsp").forward(request, response);
    }

    private void handlePaymentStep(HttpServletRequest request, HttpServletResponse response, UserSession user,
            List<Address> addresses)
            throws ServletException, IOException {
        if (addresses == null || addresses.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/checkout/address");
            return;
        }

        Address selectedAddress = resolveSelectedAddress(request, user, addresses);
        if (selectedAddress == null) {
            response.sendRedirect(request.getContextPath() + "/checkout/address");
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
        int targetAddressId = 0;
        if (addressIdParam != null && !addressIdParam.trim().isEmpty()) {
            try {
                targetAddressId = Integer.parseInt(addressIdParam.trim());
            } catch (NumberFormatException ignored) {}
        } else {
            HttpSession session = request.getSession(false);
            Integer sessionAddressId = (session != null) ? (Integer) session.getAttribute("checkoutAddressId") : null;
            if (sessionAddressId != null) {
                targetAddressId = sessionAddressId;
            }
        }

        if (targetAddressId > 0) {
            for (Address a : addresses) {
                if (a.getAddressId() == targetAddressId) {
                    return a;
                }
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

        String path = request.getServletPath();

        // 1. Step 2 (Summary -> Payment): If POST to /checkout/summary -> save delivery notes and go to /checkout/payment
        if ("/checkout/summary".equals(path)) {
            String notes = request.getParameter("notes");
            if (notes != null && !notes.trim().isEmpty()) {
                session.setAttribute("checkoutNotes", notes.trim());
            }
            response.sendRedirect(request.getContextPath() + "/checkout/payment");
            return;
        }

        // 2. Step 1 (Address -> Summary): If POST to /checkout/address -> save addressId and go to /checkout/summary
        if ("/checkout/address".equals(path)) {
            String aidParam = request.getParameter("addressId");
            if (aidParam != null && !aidParam.trim().isEmpty()) {
                try {
                    session.setAttribute("checkoutAddressId", Integer.parseInt(aidParam.trim()));
                } catch (NumberFormatException ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/checkout/summary");
            return;
        }

        // 3. If POST to /checkout without paymentMethod: check if address selected or initial Buy Now
        if ("/checkout".equals(path) && (request.getParameter("paymentMethod") == null || request.getParameter("paymentMethod").trim().isEmpty())) {
            String aidParam = request.getParameter("addressId");
            if (aidParam != null && !aidParam.trim().isEmpty()) {
                try {
                    session.setAttribute("checkoutAddressId", Integer.parseInt(aidParam.trim()));
                    response.sendRedirect(request.getContextPath() + "/checkout/summary");
                    return;
                } catch (NumberFormatException ignored) {}
            }

            String initialBuyNowPid = request.getParameter("buyNowProductId");
            if (initialBuyNowPid != null && !initialBuyNowPid.trim().isEmpty()) {
                try {
                    int pid = Integer.parseInt(initialBuyNowPid.trim());
                    int qty = 1;
                    String qtyParam = request.getParameter("quantity");
                    if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                        try { qty = Integer.parseInt(qtyParam.trim()); } catch (NumberFormatException ignored) {}
                    }
                    session.setAttribute("directBuyProductId", pid);
                    session.setAttribute("directBuyQuantity", qty);
                    response.sendRedirect(request.getContextPath() + "/checkout/address");
                    return;
                } catch (NumberFormatException ignored) {}
            }
        }

        // 4. Step 3 (Payment -> Order / Razorpay Gateway): Final Order Submission on /checkout/payment or /checkout
        String addressIdParam = request.getParameter("addressId");
        Integer sessionAddressId = (session != null) ? (Integer) session.getAttribute("checkoutAddressId") : null;
        
        int addressId = 0;
        if (addressIdParam != null && !addressIdParam.trim().isEmpty()) {
            try {
                addressId = Integer.parseInt(addressIdParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (sessionAddressId != null) {
            addressId = sessionAddressId;
        }

        String paymentMethod = request.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "UPI"; // Default to online UPI / Razorpay
        }

        String notes = request.getParameter("notes");
        if (notes == null || notes.trim().isEmpty()) {
            notes = (session != null) ? (String) session.getAttribute("checkoutNotes") : "";
        }

        Integer sessionBuyNowPid = (session != null) ? (Integer) session.getAttribute("directBuyProductId") : null;
        boolean isDirectBuy = (sessionBuyNowPid != null && sessionBuyNowPid > 0);

        if (addressId <= 0) {
            response.sendRedirect(request.getContextPath() + "/checkout/address");
            return;
        }

        try {
            Coupon sessionCoupon = (Coupon) session.getAttribute("appliedCoupon");

            Order confirmedOrder;
            if (isDirectBuy) {
                int productId = sessionBuyNowPid;
                int quantity = 1;
                if (session != null && session.getAttribute("directBuyQuantity") != null) {
                    quantity = (Integer) session.getAttribute("directBuyQuantity");
                }
                if (quantity <= 0) quantity = 1;

                confirmedOrder = orderService.processDirectBuyCheckout(user.getUserId(), addressId, productId, quantity,
                        paymentMethod, notes, sessionCoupon);
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("checkoutNotes");
                session.removeAttribute("directBuyProductId");
                session.removeAttribute("directBuyQuantity");
                session.removeAttribute("checkoutAddressId");
            } else {
                confirmedOrder = orderService.processCheckout(user.getUserId(), addressId, paymentMethod, notes,
                        sessionCoupon);

                // Clean up applied coupon & notes, and update session cart
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("checkoutNotes");
                session.removeAttribute("checkoutAddressId");
                Cart emptyCart = cartService.getCart(user.getUserId());
                session.setAttribute("cart", emptyCart);
            }

            if ("COD".equalsIgnoreCase(confirmedOrder.getPaymentMethod())) {
                OrderConfirmationContext confirmationContext = new OrderConfirmationContext(
                        confirmedOrder.getOrderId(),
                        confirmedOrder.getOrderNumber(),
                        user.getUserId(),
                        confirmedOrder.getTotalAmount(),
                        "Cash on Delivery (COD)",
                        "PENDING",
                        "COD-" + System.currentTimeMillis()
                );
                session.setAttribute("orderConfirmationContext", confirmationContext);
                session.setAttribute("justPlacedOrder", confirmedOrder);

                // Clean redirect to Order Confirmation (no URL parameters)
                response.sendRedirect(request.getContextPath() + "/order/confirmation");
            } else {
                // Online Payment (UPI / Cards / NetBanking): store order ID in session and redirect to /payment/gateway
                session.setAttribute("pendingPaymentOrderId", confirmedOrder.getOrderId());
                response.sendRedirect(request.getContextPath() + "/payment/gateway");
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