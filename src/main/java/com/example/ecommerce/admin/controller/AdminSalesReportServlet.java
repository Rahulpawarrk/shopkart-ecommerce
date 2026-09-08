package com.example.ecommerce.admin.controller;

import com.example.ecommerce.admin.model.SalesReportItem;
import com.example.ecommerce.admin.service.DashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Year;
import java.util.List;

/**
 * Controller for Administrative Sales Reports and Financial Analytics.
 * Protected by AuthFilter and RoleFilter.
 */
@WebServlet(name = "AdminSalesReportServlet", urlPatterns = {"/admin/reports/sales"})
public class AdminSalesReportServlet extends HttpServlet {

    private DashboardService dashboardService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dashboardService = new DashboardService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String yearParam = request.getParameter("year");
        int year = (yearParam != null && !yearParam.trim().isEmpty()) 
                ? Integer.parseInt(yearParam.trim()) 
                : Year.now().getValue();

        List<SalesReportItem> monthlyReport = dashboardService.getMonthlySalesReport(year);
        List<SalesReportItem> categoryReport = dashboardService.getCategoryRevenueReport();

        java.math.BigDecimal totalSettledRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalNetSales = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalTaxes = java.math.BigDecimal.ZERO;
        int totalOrdersCount = 0;
        int totalUnitsSold = 0;

        for (SalesReportItem item : monthlyReport) {
            if (item.getTotalSales() != null) {
                totalSettledRevenue = totalSettledRevenue.add(item.getTotalSales());
            }
            if (item.getNetSales() != null) {
                totalNetSales = totalNetSales.add(item.getNetSales());
            }
            if (item.getTaxAmount() != null) {
                totalTaxes = totalTaxes.add(item.getTaxAmount());
            }
            totalOrdersCount += item.getOrderCount();
        }

        for (SalesReportItem item : categoryReport) {
            totalUnitsSold += item.getUnitsSold();
        }

        java.math.BigDecimal avgOrderValue = totalOrdersCount > 0 
                ? totalSettledRevenue.divide(java.math.BigDecimal.valueOf(totalOrdersCount), 2, java.math.RoundingMode.HALF_UP) 
                : java.math.BigDecimal.ZERO;

        request.setAttribute("selectedYear", year);
        request.setAttribute("monthlyReport", monthlyReport);
        request.setAttribute("categoryReport", categoryReport);
        request.setAttribute("totalSettledRevenue", totalSettledRevenue);
        request.setAttribute("totalGrossSales", totalSettledRevenue);
        request.setAttribute("totalNetSales", totalNetSales);
        request.setAttribute("totalTaxes", totalTaxes);
        request.setAttribute("totalOrdersCount", totalOrdersCount);
        request.setAttribute("totalUnitsSold", totalUnitsSold);
        request.setAttribute("avgOrderValue", avgOrderValue);

        request.getRequestDispatcher("/index.html").forward(request, response);
    }
}

