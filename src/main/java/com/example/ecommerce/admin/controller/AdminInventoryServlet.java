package com.example.ecommerce.admin.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.inventory.model.InventoryTransaction;
import com.example.ecommerce.inventory.service.InventoryService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.example.ecommerce.util.ServletUtils;

/**
 * Controller for Administrative Inventory Management and Stock Audit History.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminInventoryServlet", urlPatterns = {
        "/admin/inventory",
        "/admin/inventory/restock",
        "/admin/inventory/adjust",
        "/admin/inventory/threshold",
        "/admin/inventory/transactions"
})
public class AdminInventoryServlet extends HttpServlet {

    private InventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.inventoryService = new InventoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if ("/admin/inventory/transactions".equals(path)) {
            showTransactionHistory(request, response);
        } else {
            showInventoryDashboard(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        switch (path) {
            case "/admin/inventory/restock" -> handleRestock(request, response);
            case "/admin/inventory/adjust" -> handleAdjustment(request, response);
            case "/admin/inventory/threshold" -> handleThresholdUpdate(request, response);
            default -> response.sendRedirect(request.getContextPath() + "/admin/inventory");
        }
    }

    private void showInventoryDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("q");
        String filter = request.getParameter("filter"); // "all", "low_stock", "out_of_stock", "in_stock"
        
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Pagination<Inventory> pagination = inventoryService.getInventoryList(keyword, filter, page, 15);
        Map<String, Integer> stats = inventoryService.getSummaryStats();

        request.setAttribute("pagination", pagination);
        request.setAttribute("stats", stats);
        request.setAttribute("keyword", keyword);
        request.setAttribute("filter", filter != null ? filter : "all");

        request.getRequestDispatcher("/WEB-INF/views/admin/inventory-list.jsp").forward(request, response);
    }

    private void showTransactionHistory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int productId;
        try {
            productId = Integer.parseInt(request.getParameter("productId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=invalid_input");
            return;
        }
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                page = Integer.parseInt(pageParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Inventory inventory = inventoryService.getProductInventory(productId);
        Pagination<InventoryTransaction> transactions = inventoryService.getTransactionHistory(productId, page, 20);

        request.setAttribute("inventory", inventory);
        request.setAttribute("transactions", transactions);

        request.getRequestDispatcher("/WEB-INF/views/admin/inventory-transactions.jsp").forward(request, response);
    }

    private void handleRestock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=session_expired");
            return;
        }

        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        int qty = ServletUtils.parseIntParam(request, "quantity", -1);
        if (productId <= 0 || qty < 0) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=Invalid+input");
            return;
        }
        String supplierRef = request.getParameter("reference");
        String remarks = request.getParameter("remarks");

        try {
            inventoryService.restockProduct(productId, qty, "SUPPLIER_RESTOCK", null, 
                    remarks != null && !remarks.trim().isEmpty() ? remarks : "Restock PO Ref: " + supplierRef, 
                    user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/admin/inventory?restocked=true");
        } catch (ValidationException ve) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=" + URLEncoder.encode(ve.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleAdjustment(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/auth/login?error=session_expired");
            return;
        }

        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        int newQty = ServletUtils.parseIntParam(request, "newQuantity", -1);
        if (productId <= 0 || newQty < 0) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=Invalid+input");
            return;
        }
        String reason = request.getParameter("remarks");

        try {
            inventoryService.adjustStock(productId, newQty, reason, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/admin/inventory?adjusted=true");
        } catch (ValidationException ve) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=" + URLEncoder.encode(ve.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleThresholdUpdate(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        int productId = ServletUtils.parseIntParam(request, "productId", -1);
        int threshold = ServletUtils.parseIntParam(request, "lowStockThreshold", -1);
        if (productId <= 0 || threshold < 0) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=Invalid+input");
            return;
        }

        inventoryService.updateLowStockThreshold(productId, threshold);
        response.sendRedirect(request.getContextPath() + "/admin/inventory?thresholdUpdated=true");
    }
}
