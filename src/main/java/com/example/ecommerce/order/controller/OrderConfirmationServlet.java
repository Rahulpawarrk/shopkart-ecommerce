package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for Order Confirmation / Success Receipt page.
 * Route: /order/confirmation?orderNumber=...
 */
@WebServlet(name = "OrderConfirmationServlet", urlPatterns = {"/order/confirmation"})
public class OrderConfirmationServlet extends HttpServlet {

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
        UserSession user = (UserSession) session.getAttribute("currentUser");

        String orderNumber = request.getParameter("orderNumber");
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            Order order = orderService.getOrderByNumber(orderNumber.trim(), user.getUserId());
            
            // Guarantee session cart and coupon are cleared
            if (session != null) {
                Cart freshCart = cartService.getCart(user.getUserId());
                session.setAttribute("cart", freshCart);
                session.removeAttribute("appliedCoupon");
            }

            request.setAttribute("order", order);
            request.getRequestDispatcher("/WEB-INF/views/order/confirmation.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }
}
