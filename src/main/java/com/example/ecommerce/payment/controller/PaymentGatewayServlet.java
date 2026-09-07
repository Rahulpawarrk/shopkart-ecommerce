package com.example.ecommerce.payment.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.model.Payment;
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
 * Controller for Payment Gateway page.
 * Route: /payment/gateway
 * Supports 100% clean address bar URLs via session-bound order context.
 * Automatically cleans query parameters on arrival via immediate redirect.
 * Protected by AuthFilter.
 */
@WebServlet(name = "PaymentGatewayServlet", urlPatterns = {"/payment/gateway"})
public class PaymentGatewayServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayServlet.class);

    private OrderService orderService;
    private PaymentService paymentService;
    private RazorpayService razorpayService;

    @Override
    public void init() throws ServletException {
        super.init();
        if (com.example.ecommerce.config.SpringContextLookup.isInitialized()) {
            this.orderService = com.example.ecommerce.config.SpringContextLookup.getBean(OrderService.class);
            this.paymentService = com.example.ecommerce.config.SpringContextLookup.getBean(PaymentService.class);
            this.razorpayService = com.example.ecommerce.config.SpringContextLookup.getBean(RazorpayService.class);
        } else {
            this.orderService = new OrderService();
            this.paymentService = new PaymentService();
            this.razorpayService = new RazorpayService();
        }
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

        // 1. If orderId is received in query parameter (e.g. ?orderId=17), store in session and REDIRECT immediately to clean URL!
        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
            try {
                int oid = Integer.parseInt(orderIdParam.trim());
                if (session != null) {
                    session.setAttribute("pendingPaymentOrderId", oid);
                }
                response.sendRedirect(request.getContextPath() + "/payment/gateway");
                return;
            } catch (NumberFormatException ignored) {}
        }

        // 2. Resolve pending order ID from session
        Integer pendingOrderId = (session != null) ? (Integer) session.getAttribute("pendingPaymentOrderId") : null;
        int orderId = (pendingOrderId != null) ? pendingOrderId : 0;

        if (orderId <= 0) {
            logger.warn("Payment Gateway accessed without valid order context by user {}", user.getUserId());
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            Order order = orderService.getOrderById(orderId, user.getUserId());
            if (order == null) {
                logger.warn("Order #{} not found or does not belong to user {}", orderId, user.getUserId());
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }

            Payment payment = paymentService.initiatePayment(order, order.getPaymentMethod());

            if (!razorpayService.isConfigured()) {
                logger.warn("Razorpay credentials not configured in environment for Order #{}", order.getOrderNumber());
                request.setAttribute("order", order);
                request.setAttribute("payment", payment);
                request.setAttribute("isRazorpayLive", false);
                request.setAttribute("paymentGatewayUnavailable", true);
                request.getRequestDispatcher("/WEB-INF/views/payment/gateway.jsp").forward(request, response);
                return;
            }

            String razorpayOrderId = razorpayService.createRazorpayOrder(order);

            request.setAttribute("order", order);
            request.setAttribute("payment", payment);
            request.setAttribute("razorpayOrderId", razorpayOrderId);
            request.setAttribute("razorpayKeyId", razorpayService.getKeyId());
            request.setAttribute("razorpayAmountInPaise", order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue());
            request.setAttribute("isRazorpayLive", true);

            request.getRequestDispatcher("/WEB-INF/views/payment/gateway.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Payment Gateway initialization failed for orderId: {}", orderId, e);
            response.sendRedirect(request.getContextPath() + "/orders");
        }
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

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
            try {
                int oid = Integer.parseInt(orderIdParam.trim());
                if (session != null) {
                    session.setAttribute("pendingPaymentOrderId", oid);
                }
            } catch (NumberFormatException ignored) {}
        }

        response.sendRedirect(request.getContextPath() + "/payment/gateway");
    }
}
