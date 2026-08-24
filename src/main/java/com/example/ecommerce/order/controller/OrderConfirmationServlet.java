package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderConfirmationContext;
import com.example.ecommerce.order.service.OrderService;
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
 * Controller for Order Confirmation / Success Receipt page.
 * Route: /order/confirmation
 * Renders confirmation receipt using secure session-bound context to prevent address-bar leakage.
 * Protected by AuthFilter.
 */
@WebServlet(name = "OrderConfirmationServlet", urlPatterns = {"/order/confirmation", "/order/confirmed"})
public class OrderConfirmationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(OrderConfirmationServlet.class);

    private OrderService orderService;
    private CartService cartService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderService = new OrderService();
        this.cartService = new CartService();
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

        // 1. First check session-bound OrderConfirmationContext (Clean URL pattern)
        OrderConfirmationContext context = (session != null) 
                ? (OrderConfirmationContext) session.getAttribute("orderConfirmationContext") 
                : null;

        Order order = null;
        if (context != null) {
            try {
                order = orderService.getOrderById(context.getOrderId(), user.getUserId());
            } catch (Exception e) {
                logger.warn("Could not load order {} from confirmation context for user {}", context.getOrderId(), user.getUserId(), e);
            }
        }

        // 2. Fallback: Check for query parameters (e.g. from direct links or email receipt click)
        if (order == null) {
            String orderNum = request.getParameter("orderNumber");
            if (orderNum == null || orderNum.trim().isEmpty()) {
                orderNum = request.getParameter("id");
            }
            if (orderNum == null || orderNum.trim().isEmpty()) {
                orderNum = request.getParameter("orderId");
            }

            if (orderNum != null && !orderNum.trim().isEmpty()) {
                try {
                    order = orderService.getOrderByIdOrNumber(orderNum.trim(), user.getUserId());
                } catch (Exception e) {
                    logger.warn("Could not find order {} for user {}", orderNum, user.getUserId());
                }
            }
        }

        if (order == null) {
            logger.info("Order confirmation accessed without valid order context by user {}", user.getUserId());
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            // Guarantee session cart and coupon are refreshed/cleared
            if (session != null) {
                Cart freshCart = cartService.getCart(user.getUserId());
                session.setAttribute("cart", freshCart);
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("justPlacedOrder");
            }

            request.setAttribute("order", order);
            request.setAttribute("confirmationContext", context);
            request.getRequestDispatcher("/WEB-INF/views/order/confirmation.jsp").forward(request, response);

        } catch (Exception e) {
            logger.error("Error rendering order confirmation for user {}", user.getUserId(), e);
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }
}
