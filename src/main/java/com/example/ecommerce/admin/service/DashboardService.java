package com.example.ecommerce.admin.service;

import com.example.ecommerce.admin.dao.DashboardDAO;
import com.example.ecommerce.admin.model.DashboardStats;
import com.example.ecommerce.admin.model.SalesReportItem;
import java.time.Year;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer aggregating executive dashboard KPIs and multi-dimensional
 * financial reports.
 */
@Service
@Transactional
public class DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    public DashboardService(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    public DashboardStats getDashboardKPIs() {
        return dashboardDAO.getDashboardKPIs();
    }

    public List<SalesReportItem> getMonthlySalesReport(Integer year) {
        int targetYear = (year != null && year > 2000) ? year : Year.now().getValue();
        return dashboardDAO.getMonthlySalesReport(targetYear);
    }

    public List<SalesReportItem> getCategoryRevenueReport() {
        return dashboardDAO.getCategoryRevenueReport();
    }

    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        return dashboardDAO.getTopSellingProducts(limit > 0 ? limit : 5);
    }
}
