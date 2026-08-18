package com.example.ecommerce.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Line item entry for financial and product sales breakdown reports.
 */
public class SalesReportItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private int orderCount = 0;
    private int unitsSold = 0;
    private BigDecimal totalSales = BigDecimal.ZERO;

    public SalesReportItem() {}

    public SalesReportItem(String label, int orderCount, int unitsSold, BigDecimal totalSales) {
        this.label = label;
        this.orderCount = orderCount;
        this.unitsSold = unitsSold;
        this.totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getUnitsSold() {
        return unitsSold;
    }

    public void setUnitsSold(int unitsSold) {
        this.unitsSold = unitsSold;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
    }
}
