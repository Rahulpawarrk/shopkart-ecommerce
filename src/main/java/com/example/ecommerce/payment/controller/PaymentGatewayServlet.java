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
 * Controller for Interactive Simulated Payment Gateway page.
 * Route: /payment/gateway?orderId=...
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
        this.orderService = new OrderService();
        this.paymentService = new PaymentService();
        this.razorpayService = new RazorpayService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (UserSession) session.getAttribute("currentUser");

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdParam.trim());
            Order order = orderService.getOrderById(orderId, user.getUserId());
            Payment payment = paymentService.initiatePayment(order, order.getPaymentMethod());
            String razorpayOrderId = razorpayService.createRazorpayOrder(order);

            request.setAttribute("order", order);
            request.setAttribute("payment", payment);
            request.setAttribute("razorpayOrderId", razorpayOrderId);
            request.setAttribute("razorpayKeyId", razorpayService.getKeyId());
            request.setAttribute("razorpayAmountInPaise", order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue());
            request.setAttribute("isRazorpayLive", razorpayService.isConfigured());

            request.getRequestDispatcher("/WEB-INF/views/payment/gateway.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Payment Gateway initialization failed for orderId: {}", orderIdParam, e);
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }
}
