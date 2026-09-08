package com.example.ecommerce.api.dto;

import com.example.ecommerce.admin.model.DashboardStats;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminDashboardStatsDto {
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private int totalOrders;
    private int todayOrders;
    private int pendingOrders;
    private int totalProducts;
    private int lowStockProducts;
    private int totalCustomers;
    private List<OrderDto> recentOrders = new ArrayList<>();
    private List<Map<String, Object>> topSellingProducts = new ArrayList<>();

    public AdminDashboardStatsDto() {}

    public static AdminDashboardStatsDto from(DashboardStats stats) {
        if (stats == null) return new AdminDashboardStatsDto();
        AdminDashboardStatsDto dto = new AdminDashboardStatsDto();
        dto.setTotalRevenue(stats.getTotalRevenue());
        dto.setTodayRevenue(stats.getTodayRevenue());
        dto.setTotalOrders(stats.getTotalOrders());
        dto.setTodayOrders(stats.getTodayOrders());
        dto.setPendingOrders(stats.getPendingOrders());
        dto.setTotalProducts(stats.getTotalProducts());
        dto.setLowStockProducts(stats.getLowStockProducts());
        dto.setTotalCustomers(stats.getTotalCustomers());
        return dto;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(int todayOrders) {
        this.todayOrders = todayOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(int lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public List<OrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<Map<String, Object>> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<Map<String, Object>> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }
}
