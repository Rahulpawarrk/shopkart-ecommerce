package com.example.ecommerce.admin.service;

import com.example.ecommerce.admin.dao.DashboardDAO;
import com.example.ecommerce.admin.model.DashboardStats;
import com.example.ecommerce.admin.model.SalesReportItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests with Mockito")
class DashboardServiceTest {

    @Mock
    private DashboardDAO dashboardDAO;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should successfully load executive KPI stats")
    void testGetDashboardKPIs() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalRevenue(new BigDecimal("150000.00"));
        stats.setTotalOrders(45);
        stats.setTotalProducts(120);

        when(dashboardDAO.getDashboardKPIs()).thenReturn(stats);

        DashboardStats result = dashboardService.getDashboardKPIs();

        assertNotNull(result);
        assertEquals(new BigDecimal("150000.00"), result.getTotalRevenue());
        assertEquals(45, result.getTotalOrders());
        assertEquals(120, result.getTotalProducts());
    }

    @Test
    @DisplayName("Should retrieve monthly sales report for target year")
    void testGetMonthlySalesReport() {
        SalesReportItem item = new SalesReportItem("2026-08", 10, 25, new BigDecimal("25000.00"));
        when(dashboardDAO.getMonthlySalesReport(2026)).thenReturn(Collections.singletonList(item));

        List<SalesReportItem> report = dashboardService.getMonthlySalesReport(2026);

        assertNotNull(report);
        assertEquals(1, report.size());
        assertEquals("2026-08", report.get(0).getLabel());
    }
}
