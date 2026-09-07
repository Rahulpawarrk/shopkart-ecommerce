package com.example.ecommerce.payment.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderConfirmationContext;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.model.PaymentFailureContext;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.payment.service.RazorpayService;
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
 * Production Controller handling Razorpay Payment Gateway Webhooks/Callbacks.
 * Route: /payment/callback
 * Implements cryptographic signature verification, server-side fetch & capture validation,
 * and zero address-bar leakage redirects.
 */
@WebServlet(name = "PaymentCallbackServlet", urlPatterns = { "/payment/callback", "/api/payment/webhook" })
public class PaymentCallbackServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackServlet.class);

    private PaymentService paymentService;
    private OrderService orderService;
    private CartService cartService;
    private RazorpayService razorpayService;

    @Override
    public void init() throws ServletException {
        super.init();
        if (com.example.ecommerce.config.SpringContextLookup.isInitialized()) {
            this.paymentService = com.example.ecommerce.config.SpringContextLookup.getBean(PaymentService.class);
            this.orderService = com.example.ecommerce.config.SpringContextLookup.getBean(OrderService.class);
            this.cartService = com.example.ecommerce.config.SpringContextLookup.getBean(CartService.class);
            this.razorpayService = com.example.ecommerce.config.SpringContextLookup.getBean(RazorpayService.class);
        } else {
            this.paymentService = new PaymentService();
            this.orderService = new OrderService();
            this.cartService = new CartService();
            this.razorpayService = new RazorpayService();
        }

        logger.info("PaymentCallbackServlet initialized. Razorpay configured={}", razorpayService.isConfigured());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        boolean isWebhook = "/api/payment/webhook".equalsIgnoreCase(path);

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (user == null && !isWebhook) {
            logger.warn("Payment callback rejected: unauthenticated request");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 1. Read Order ID from POST body
        String orderIdParam = request.getParameter("orderId");
        if (isBlank(orderIdParam)) {
            logger.warn("Payment callback rejected: missing orderId.");
            if (isWebhook) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"missing orderId\"}");
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid orderId received: {}", orderIdParam);
            if (isWebhook) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
            return;
        }

        // 2. Load order from Database
        final Order order;
        try {
            if (user != null) {
                order = orderService.getOrderById(orderId, user.getUserId());
            } else {
                // Webhook mode: load order by id directly
                order = orderService.getAdminOrderById(orderId);
            }
        } catch (Exception e) {
            logger.error("Unable to load order {}", orderId, e);
            if (isWebhook) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
            return;
        }

        if (order == null) {
            logger.warn("Order {} not found", orderId);
            if (isWebhook) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }
            return;
        }

        // 3. Read Razorpay response parameters
        String rzpPaymentId = trimToNull(request.getParameter("razorpay_payment_id"));
        String rzpOrderId = trimToNull(request.getParameter("razorpay_order_id"));
        String rzpSignature = trimToNull(request.getParameter("razorpay_signature"));
        String clientStatus = trimToNull(request.getParameter("status"));
        String clientReason = trimToNull(request.getParameter("reason"));
        String transactionReference = trimToNull(request.getParameter("transactionReference"));

        // Webhook processing branch
        if (isWebhook) {
            handleServerWebhook(request, response, order, rzpPaymentId, rzpOrderId, rzpSignature);
            return;
        }

        // 4. Handle client-initiated cancellation or window dismissal
        if ("FAILED".equalsIgnoreCase(clientStatus) || "CANCELLED".equalsIgnoreCase(clientStatus)) {
            String failureReason = isBlank(clientReason) ? "Customer cancelled or dismissed payment window" : clientReason;
            try {
                paymentService.handlePaymentFailure(order.getOrderId(), failureReason);
            } catch (Exception e) {
                logger.warn("Error recording payment cancellation for order {}", order.getOrderId(), e);
            }

            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith) || "true".equalsIgnoreCase(request.getParameter("ajax"))) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"Payment cancellation recorded\"}");
                return;
            }

            redirectPaymentFailure(request, response, order, failureReason);
            return;
        }

        // 5. Branch: Real Razorpay vs Development Simulator
        if (razorpayService.isConfigured()) {
            handleRealRazorpayPayment(request, response, user, order, rzpPaymentId, rzpOrderId, rzpSignature, transactionReference);
        } else {
            handleDevelopmentSimulation(request, response, user, order, clientStatus, clientReason, transactionReference);
        }
    }

    private void handleServerWebhook(HttpServletRequest request, HttpServletResponse response,
                                     Order order, String rzpPaymentId, String rzpOrderId, String rzpSignature) throws IOException {
        response.setContentType("application/json");
        if (isBlank(rzpPaymentId) || isBlank(rzpOrderId) || isBlank(rzpSignature)) {
            logger.warn("Webhook rejected: missing cryptographic signature or IDs for order #{}", order.getOrderNumber());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Missing required signature or IDs\"}");
            return;
        }

        boolean signatureValid = razorpayService.verifyPaymentSignature(rzpOrderId, rzpPaymentId, rzpSignature);
        if (!signatureValid) {
            logger.error("Webhook signature verification failed for order #{}", order.getOrderNumber());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid signature\"}");
            return;
        }

        boolean processed = paymentService.processServerWebhook(
                order.getOrderId(),
                rzpPaymentId,
                rzpOrderId,
                true,
                "Webhook verified payment capture. ID: " + rzpPaymentId
        );

        if (processed) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"ok\",\"orderId\":" + order.getOrderId() + "}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Processing failed\"}");
        }
    }

    /**
     * Handles real production Razorpay payment with cryptographic HMAC verification and server-side validation.
     */
    private void handleRealRazorpayPayment(
            HttpServletRequest request,
            HttpServletResponse response,
            UserSession user,
            Order order,
            String razorpayPaymentId,
            String razorpayOrderId,
            String razorpaySignature,
            String transactionReference)
            throws IOException {

        // Check required fields
        if (isBlank(razorpayPaymentId) || isBlank(razorpayOrderId) || isBlank(razorpaySignature)) {
            logger.info("Payment attempt aborted or incomplete for order #{}. userId={}", order.getOrderNumber(), user.getUserId());
            redirectPaymentFailure(request, response, order, "Incomplete payment response received from Razorpay.");
            return;
        }

        // 1. Cryptographic Signature Verification
        boolean signatureValid = razorpayService.verifyPaymentSignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        if (!signatureValid) {
            logger.error("Razorpay signature verification FAILED for order #{} (RZP Order: {}, Payment: {})",
                    order.getOrderNumber(), razorpayOrderId, razorpayPaymentId);
            redirectPaymentFailure(request, response, order, "Payment verification failed. Cryptographic signature is invalid.");
            return;
        }

        // 2. Server-side payment fetch and validation
        boolean serverVerified = razorpayService.verifyAndFetchPayment(razorpayPaymentId, razorpayOrderId, order.getTotalAmount());
        if (!serverVerified) {
            logger.error("Razorpay server-side verification FAILED for order #{} (Payment ID: {})",
                    order.getOrderNumber(), razorpayPaymentId);
            redirectPaymentFailure(request, response, order, "Server-side payment verification failed. Please contact support.");
            return;
        }

        // 3. Process Success in Database
        try {
            String txnRef = (transactionReference != null) ? transactionReference : razorpayPaymentId;
            boolean processed = paymentService.processGatewayCallback(
                    order.getOrderId(),
                    user.getUserId(),
                    txnRef,
                    razorpayOrderId,
                    true,
                    "Razorpay payment verified & captured. Payment ID: " + razorpayPaymentId
            );

            if (!processed) {
                logger.warn("Razorpay payment could not be recorded for order #{}", order.getOrderNumber());
                redirectPaymentFailure(request, response, order, "Payment could not be recorded in database.");
                return;
            }

            // Refresh cart in session
            refreshCartAfterSuccessfulPayment(sessionFrom(request), user);

            // Clean session-based flash redirect to Order Confirmation (NO order number in URL!)
            OrderConfirmationContext confirmationContext = new OrderConfirmationContext(
                    order.getOrderId(),
                    order.getOrderNumber(),
                    user.getUserId(),
                    order.getTotalAmount(),
                    "ONLINE (Razorpay)",
                    "PAID",
                    txnRef
            );
            sessionFrom(request).setAttribute("orderConfirmationContext", confirmationContext);
            sessionFrom(request).removeAttribute("pendingPaymentOrderId");

            response.sendRedirect(request.getContextPath() + "/orders");

        } catch (Exception e) {
            logger.error("Error finalizing verified Razorpay payment for order #{}", order.getOrderNumber(), e);
            redirectPaymentFailure(request, response, order, "Payment succeeded but order recording encountered an error.");
        }
    }

    /**
     * Handles sandbox simulation mode for local development.
     */
    private void handleDevelopmentSimulation(
            HttpServletRequest request,
            HttpServletResponse response,
            UserSession user,
            Order order,
            String clientStatus,
            String clientReason,
            String transactionReference)
            throws IOException {

        // Strict Production Protection: disallow client-side status simulation in production environments
        String env = System.getenv("APP_ENV");
        if ("production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env)) {
            logger.error("Security Alert: Sandbox simulation attempted in production environment for order #{}", order.getOrderNumber());
            redirectPaymentFailure(request, response, order, "Online payment processing is temporarily unavailable. Please retry or contact support.");
            return;
        }

        boolean success = "SUCCESS".equalsIgnoreCase(clientStatus);
        String txnRef = (transactionReference != null) ? transactionReference : "SIM-" + System.currentTimeMillis();
        String gatewayMessage = success
                ? "Sandbox Simulation Successful. Txn: " + txnRef
                : (clientReason != null ? clientReason : "Sandbox Payment Declined.");

        try {
            boolean processed = paymentService.processGatewayCallback(
                    order.getOrderId(),
                    user.getUserId(),
                    txnRef,
                    null,
                    success,
                    gatewayMessage
            );

            if (processed && success) {
                refreshCartAfterSuccessfulPayment(sessionFrom(request), user);

                OrderConfirmationContext confirmationContext = new OrderConfirmationContext(
                        order.getOrderId(),
                        order.getOrderNumber(),
                        user.getUserId(),
                        order.getTotalAmount(),
                        "ONLINE (Sandbox)",
                        "PAID",
                        txnRef
                );
                sessionFrom(request).setAttribute("orderConfirmationContext", confirmationContext);
                sessionFrom(request).removeAttribute("pendingPaymentOrderId");

                response.sendRedirect(request.getContextPath() + "/orders");

            } else {
                redirectPaymentFailure(request, response, order, gatewayMessage);
            }

        } catch (Exception e) {
            logger.error("Sandbox simulation processing failed for order #{}", order.getOrderNumber(), e);
            redirectPaymentFailure(request, response, order, "Unable to process simulated payment.");
        }
    }

    private void refreshCartAfterSuccessfulPayment(HttpSession session, UserSession user) {
        if (session == null) return;
        try {
            Cart freshCart = cartService.getCart(user.getUserId());
            session.setAttribute("cart", freshCart);
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("justPlacedOrder");
        } catch (Exception e) {
            logger.warn("Unable to refresh cart after payment for user {}", user.getUserId(), e);
        }
    }

    private void redirectPaymentFailure(HttpServletRequest request, HttpServletResponse response, Order order, String message)
            throws IOException {
        try {
            paymentService.handlePaymentFailure(order.getOrderId(), message);
        } catch (Exception e) {
            logger.warn("Failed to record payment failure state for order {}", order.getOrderId(), e);
        }

        HttpSession session = sessionFrom(request);
        if (session != null) {
            PaymentFailureContext failureContext = new PaymentFailureContext(
                    order.getOrderId(),
                    order.getOrderNumber(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getPaymentMethod(),
                    message,
                    null,
                    true,
                    true
            );
            session.setAttribute("paymentFailureContext", failureContext);
        }

        // Clean redirect without query parameters in the address bar!
        response.sendRedirect(request.getContextPath() + "/payment/failure");
    }

    private HttpSession sessionFrom(HttpServletRequest request) {
        return request.getSession(false);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}