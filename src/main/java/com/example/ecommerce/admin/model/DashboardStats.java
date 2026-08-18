package com.example.ecommerce.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * High-level executive KPI metrics for the Admin Dashboard.
 */
public class DashboardStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal todayRevenue = BigDecimal.ZERO;
    private int totalOrders = 0;
    private int todayOrders = 0;
    private int pendingOrders = 0;
    private int totalProducts = 0;
    private int lowStockProducts = 0;
    private int totalCustomers = 0;

    public DashboardStats() {}

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
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
}
