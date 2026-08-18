package com.example.ecommerce.admin.controller;

import com.example.ecommerce.payment.model.Payment;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for Administrative Payment & Transaction Logs.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminPaymentServlet", urlPatterns = {"/admin/payments"})
public class AdminPaymentServlet extends HttpServlet {

    private PaymentService paymentService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.paymentService = new PaymentService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("q");
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<Payment> pagination = paymentService.getAllPayments(keyword, page, 15);
        Map<String, Object> stats = paymentService.getPaymentSummaryStats();

        request.setAttribute("pagination", pagination);
        request.setAttribute("stats", stats);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/WEB-INF/views/admin/payment-list.jsp").forward(request, response);
    }
}
