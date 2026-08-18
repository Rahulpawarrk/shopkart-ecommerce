package com.example.ecommerce.admin.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for Administrative Order Management and Status Transitions.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminOrderServlet", urlPatterns = {
        "/admin/orders",
        "/admin/orders/detail",
        "/admin/orders/status"
})
public class AdminOrderServlet extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/admin/orders/detail".equals(path)) {
            showOrderDetail(request, response);
        } else {
            showOrderList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/admin/orders/status".equals(path)) {
            handleStatusUpdate(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/orders");
        }
    }

    private void showOrderList(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("q");
        String statusParam = request.getParameter("status");
        OrderStatus status = null;
        if (statusParam != null && !statusParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusParam)) {
            try {
                status = OrderStatus.valueOf(statusParam.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<Order> pagination = orderService.getAllOrders(keyword, status, page, 15);
        Map<String, Object> stats = orderService.getOrderSummaryStats();

        request.setAttribute("pagination", pagination);
        request.setAttribute("stats", stats);
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status != null ? status.name() : "ALL");

        request.getRequestDispatcher("/WEB-INF/views/admin/order-list.jsp").forward(request, response);
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int orderId = Integer.parseInt(request.getParameter("id"));
        Order order = orderService.getAdminOrderById(orderId);
        request.setAttribute("order", order);

        request.getRequestDispatcher("/WEB-INF/views/admin/order-detail.jsp").forward(request, response);
    }

    private void handleStatusUpdate(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession adminUser = (UserSession) session.getAttribute("currentUser");

        int orderId = Integer.parseInt(request.getParameter("orderId"));
        OrderStatus newStatus = OrderStatus.valueOf(request.getParameter("newStatus"));
        String remarks = request.getParameter("remarks");
        String courierPartner = request.getParameter("courierPartner");
        String trackingNumber = request.getParameter("trackingNumber");
        String deliveryAgentPhone = request.getParameter("deliveryAgentPhone");

        try {
            orderService.updateOrderStatusByAdmin(orderId, newStatus, remarks, adminUser.getUserId(), courierPartner, trackingNumber, deliveryAgentPhone);
            response.sendRedirect(request.getContextPath() + "/admin/orders/detail?id=" + orderId + "&updated=true");
        } catch (com.example.ecommerce.exception.ValidationException ve) {
            response.sendRedirect(request.getContextPath() + "/admin/orders/detail?id=" + orderId + "&error=" + java.net.URLEncoder.encode(ve.getMessage(), "UTF-8"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/admin/orders/detail?id=" + orderId + "&error=" + java.net.URLEncoder.encode("Failed to update status: " + e.getMessage(), "UTF-8"));
        }
    }
}
