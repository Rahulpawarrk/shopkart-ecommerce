package com.example.ecommerce.admin.controller;

import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.audit.service.AuditLogService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller for Administrative Security & System Audit Trail.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminAuditLogServlet", urlPatterns = {"/admin/audit-logs"})
public class AdminAuditLogServlet extends HttpServlet {

    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        String entityName = request.getParameter("entity");
        String keyword = request.getParameter("q");

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<AuditLog> pagination = auditLogService.getAuditLogs(action, entityName, keyword, page, 20);

        request.setAttribute("pagination", pagination);
        request.setAttribute("action", action);
        request.setAttribute("entity", entityName);
        request.setAttribute("keyword", keyword);

        request.getRequestDispatcher("/WEB-INF/views/admin/audit-list.jsp").forward(request, response);
    }
}
