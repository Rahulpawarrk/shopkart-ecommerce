package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller for Customer Order History, Order Details, and Order Cancellations.
 * Routes: /orders, /order, /order/cancel
 * Protected by AuthFilter.
 */
@WebServlet(name = "CustomerOrderServlet", urlPatterns = {
        "/orders",
        "/order",
        "/order/cancel"
})
public class CustomerOrderServlet extends HttpServlet {

    private OrderService orderService;
    private com.example.ecommerce.order.service.OrderReturnService orderReturnService;
    private com.example.ecommerce.review.service.ReviewService reviewService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderService = new OrderService();
        this.orderReturnService = new com.example.ecommerce.order.service.OrderReturnService();
        this.reviewService = new com.example.ecommerce.review.service.ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/orders");
            return;
        }

        String path = request.getServletPath();

        if ("/order".equals(path)) {
            showOrderDetail(request, response);
        } else {
            showOrderList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/admin/orders");
            return;
        }

        String path = request.getServletPath();

        if ("/order/cancel".equals(path)) {
            handleCancelOrder(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }

    private void showOrderList(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        String status = request.getParameter("status");
        String sortBy = request.getParameter("sort");

        Pagination<Order> pagination = orderService.getUserOrders(user.getUserId(), status, sortBy, page, 10);
        request.setAttribute("pagination", pagination);
        request.setAttribute("selectedStatus", status != null ? status.trim() : "ALL");
        request.setAttribute("selectedSort", sortBy != null ? sortBy.trim() : "newest");

        // Flash attribute check for freshly placed order confirmation modal
        if (session != null) {
            com.example.ecommerce.order.model.OrderConfirmationContext confirmationContext = 
                    (com.example.ecommerce.order.model.OrderConfirmationContext) session.getAttribute("orderConfirmationContext");
            if (confirmationContext != null) {
                request.setAttribute("confirmationContext", confirmationContext);
                request.setAttribute("showOrderSuccessModal", true);
                session.removeAttribute("orderConfirmationContext");
            }
        }

        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("num");
        }
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("orderNumber");
        }

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }

        try {
            Order order = orderService.getOrderByIdOrNumber(idParam.trim(), user.getUserId());
            request.setAttribute("order", order);

            orderReturnService.getReturnByOrderId(order.getOrderId())
                    .ifPresent(ret -> request.setAttribute("orderReturn", ret));

            // Load existing customer reviews for items in this order
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                java.util.Map<Integer, com.example.ecommerce.review.model.Review> userReviews = new java.util.HashMap<>();
                for (com.example.ecommerce.order.model.OrderItem item : order.getItems()) {
                    reviewService.getUserReviewForProduct(user.getUserId(), item.getProductId())
                            .ifPresent(r -> userReviews.put(item.getProductId(), r));
                }
                request.setAttribute("userReviews", userReviews);
            }

            request.getRequestDispatcher("/index.html").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=notfound");
        }
    }

    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=auth_required");
            return;
        }

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            orderIdParam = request.getParameter("id");
        }
        String reason = request.getParameter("reason");

        try {
            Order order = orderService.getOrderByIdOrNumber(orderIdParam.trim(), user.getUserId());
            orderService.cancelOrder(order.getOrderId(), user.getUserId(), reason);
            response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderNumber() + "&cancelled=true");
        } catch (ValidationException ve) {
            response.sendRedirect(request.getContextPath() + "/order?id=" + (orderIdParam != null ? orderIdParam : "") + "&error=" + ve.getMessage());
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders?error=cancel_failed");
        }
    }
}

