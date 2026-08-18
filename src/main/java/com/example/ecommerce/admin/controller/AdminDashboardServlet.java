package com.example.ecommerce.admin.controller;

import com.example.ecommerce.admin.model.DashboardStats;
import com.example.ecommerce.admin.service.DashboardService;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for Administrative Dashboard.
 * Protected by AuthFilter and RoleFilter (requires ADMIN role).
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private DashboardService dashboardService;
    private OrderDAO orderDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dashboardService = new DashboardService();
        this.orderDAO = new OrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession userSession = (UserSession) session.getAttribute("currentUser");

        DashboardStats stats = dashboardService.getDashboardKPIs();
        Pagination<Order> recentOrders = orderDAO.findAll(null, null, 1, 5);
        List<Map<String, Object>> topProducts = dashboardService.getTopSellingProducts(5);

        request.setAttribute("adminName", userSession.getFullName());
        request.setAttribute("adminEmail", userSession.getEmail());
        request.setAttribute("stats", stats);
        request.setAttribute("recentOrders", recentOrders.getItems());
        request.setAttribute("topProducts", topProducts);

        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
