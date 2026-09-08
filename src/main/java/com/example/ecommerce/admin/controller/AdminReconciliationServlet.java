package com.example.ecommerce.admin.controller;

import com.example.ecommerce.audit.service.AuditLogService;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.payment.model.PaymentReconciliation;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Administrative Controller managing Failed Payment Transactions & Reconciliation Ledger.
 * Used for investigating customer debit queries, logging bank refund references, and manual settlements.
 */
@WebServlet(name = "AdminReconciliationServlet", urlPatterns = {"/admin/reconciliation", "/admin/reconciliation/status"})
public class AdminReconciliationServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminReconciliationServlet.class);

    private PaymentService paymentService;
    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.paymentService = new PaymentService();
        this.auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String servletPath = request.getServletPath();
        String requestUri = request.getRequestURI();
        if ((servletPath != null && servletPath.endsWith("/status")) || (requestUri != null && requestUri.endsWith("/status"))) {
            response.sendRedirect(request.getContextPath() + "/admin/reconciliation");
            return;
        }

        String keyword = request.getParameter("q");
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            status = "ALL";
        }

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<PaymentReconciliation> pagination = paymentService.getReconciliationList(keyword, status, page, 15);
        Map<String, Object> stats = paymentService.getReconciliationStats();

        request.setAttribute("pagination", pagination);
        request.setAttribute("stats", stats);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentStatus", status);

        if (request.getParameter("updated") != null) {
            request.setAttribute("successMessage", "Payment reconciliation status & resolution notes updated successfully.");
        }

        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession currentUser = session != null ? (UserSession) session.getAttribute("currentUser") : null;
        if (currentUser == null || !currentUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Administrator privileges required.");
            return;
        }

        String reconIdParam = request.getParameter("reconciliationId");
        String newStatus = request.getParameter("reconciliationStatus");
        String adminNotes = request.getParameter("adminNotes");

        if (reconIdParam == null || reconIdParam.trim().isEmpty() || newStatus == null || newStatus.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/reconciliation?error=invalid_params");
            return;
        }

        try {
            int reconciliationId = Integer.parseInt(reconIdParam.trim());
            boolean success = paymentService.updateReconciliationResolution(
                    reconciliationId, 
                    newStatus.trim(), 
                    adminNotes != null ? adminNotes.trim() : "", 
                    currentUser.getUserId()
            );

            if (success) {
                auditLogService.logAction(
                        currentUser.getUserId(),
                        "RECONCILE_PAYMENT_FAILURE",
                        "payment_reconciliation",
                        reconciliationId,
                        null,
                        "Status updated to: " + newStatus + " | Notes: " + adminNotes,
                        request.getRemoteAddr()
                );
                logger.info("Admin [{}] updated payment reconciliation record [{}] to status [{}]", 
                        currentUser.getEmail(), reconciliationId, newStatus);
            }

            response.sendRedirect(request.getContextPath() + "/admin/reconciliation?updated=true");
        } catch (Exception e) {
            logger.error("Error updating payment reconciliation record", e);
            response.sendRedirect(request.getContextPath() + "/admin/reconciliation?error=update_failed");
        }
    }
}

