package com.example.ecommerce.payment.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderConfirmationContext;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.model.PaymentFailureContext;
import com.example.ecommerce.payment.service.PaymentService;
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
 * Production Controller for Payment Failure handling, retries, and switching to Cash on Delivery.
 * Route: /payment/failure, /payment/failure/action
 * Protected by AuthFilter.
 */
@WebServlet(name = "PaymentFailureServlet", urlPatterns = { "/payment/failure", "/payment/failure/action" })
public class PaymentFailureServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PaymentFailureServlet.class);

    private OrderService orderService;
    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderService = new OrderService();
        this.paymentService = new PaymentService();
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

        PaymentFailureContext failureContext = (session != null) 
                ? (PaymentFailureContext) session.getAttribute("paymentFailureContext") 
                : null;

        if (failureContext == null) {
            // Check if orderId was passed as param (fallback for direct links)
            String orderIdParam = request.getParameter("orderId");
            if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
                try {
                    int orderId = Integer.parseInt(orderIdParam.trim());
                    Order order = orderService.getOrderById(orderId, user.getUserId());
                    String reason = request.getParameter("error");
                    failureContext = new PaymentFailureContext(
                            order.getOrderId(),
                            order.getOrderNumber(),
                            user.getUserId(),
                            order.getTotalAmount(),
                            order.getPaymentMethod(),
                            reason != null ? reason : "Transaction was declined or cancelled.",
                            null,
                            true,
                            true
                    );
                } catch (Exception ignored) {
                    response.sendRedirect(request.getContextPath() + "/orders");
                    return;
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
                return;
            }
        }

        try {
            Order order = orderService.getOrderById(failureContext.getOrderId(), user.getUserId());
            request.setAttribute("order", order);
            request.setAttribute("failureContext", failureContext);
            request.setAttribute("error", failureContext.getFailureReason());

            request.getRequestDispatcher("/index.html").forward(request, response);
        } catch (Exception e) {
            logger.error("Error loading payment failure page for user {}", user.getUserId(), e);
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (session == null || user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String action = request.getParameter("action");
        String orderIdParam = request.getParameter("orderId");

        int orderId = 0;
        PaymentFailureContext failureContext = (PaymentFailureContext) session.getAttribute("paymentFailureContext");

        if (failureContext != null) {
            orderId = failureContext.getOrderId();
        } else if (orderIdParam != null && !orderIdParam.trim().isEmpty()) {
            try {
                orderId = Integer.parseInt(orderIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (orderId <= 0) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            Order order = orderService.getOrderById(orderId, user.getUserId());

            if ("switch_cod".equalsIgnoreCase(action)) {
                // Switch order to COD
                paymentService.switchPaymentMethodToCod(order.getOrderId(), user.getUserId());
                session.removeAttribute("paymentFailureContext");

                // Store confirmation flash context and redirect cleanly
                OrderConfirmationContext confirmationContext = new OrderConfirmationContext(
                        order.getOrderId(),
                        order.getOrderNumber(),
                        user.getUserId(),
                        order.getTotalAmount(),
                        "COD",
                        "PENDING",
                        "COD-" + System.currentTimeMillis()
                );
                session.setAttribute("orderConfirmationContext", confirmationContext);
                response.sendRedirect(request.getContextPath() + "/orders");

            } else if ("retry".equalsIgnoreCase(action)) {
                // Prepare session for clean gateway redirect
                session.removeAttribute("paymentFailureContext");
                session.setAttribute("pendingPaymentOrderId", order.getOrderId());
                response.sendRedirect(request.getContextPath() + "/payment/gateway");

            } else {
                response.sendRedirect(request.getContextPath() + "/orders");
            }

        } catch (Exception e) {
            logger.error("Failed to process payment failure action [{}] for order {}", action, orderId, e);
            response.sendRedirect(request.getContextPath() + "/orders?error=action_failed");
        }
    }
}

