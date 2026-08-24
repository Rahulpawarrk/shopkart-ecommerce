package com.example.ecommerce.admin.controller;

import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.service.OrderReturnService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for Admin Order Return and Replacement Moderation.
 * Route: /admin/returns
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminReturnServlet", urlPatterns = {"/admin/returns"})
public class AdminReturnServlet extends HttpServlet {

    private OrderReturnService orderReturnService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderReturnService = new OrderReturnService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("q");
        String status = request.getParameter("status");
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try { page = Integer.parseInt(pageParam.trim()); } catch (NumberFormatException ignored) {}
        }

        Pagination<OrderReturn> returns = orderReturnService.getAllReturns(keyword, status, page, 15);
        request.setAttribute("returns", returns);
        request.setAttribute("keyword", keyword);
        request.setAttribute("selectedStatus", status != null ? status : "ALL");

        request.getRequestDispatcher("/WEB-INF/views/admin/return-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int returnId = ServletUtils.parseIntParam(request, "returnId", -1);
        if (returnId <= 0) {
            response.sendRedirect(request.getContextPath() + "/admin/returns?error=invalid_id");
            return;
        }
        String newStatus = request.getParameter("returnStatus");
        String adminNotes = request.getParameter("adminNotes");
        String refundAmountStr = request.getParameter("refundAmount");

        BigDecimal refundAmount = null;
        if (refundAmountStr != null && !refundAmountStr.trim().isEmpty()) {
            try {
                refundAmount = new BigDecimal(refundAmountStr.trim());
            } catch (Exception ignored) {}
        }

        orderReturnService.updateReturnStatus(returnId, newStatus, adminNotes, refundAmount);
        response.sendRedirect(request.getContextPath() + "/admin/returns?updated=true");
    }
}
