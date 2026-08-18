package com.example.ecommerce.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Encapsulates search, filtering, sorting, and pagination parameters for product queries.
 */
public class ProductSearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minDiscount;
    private boolean dealsOnly = false;
    private String brand;
    private java.util.List<String> brands = new java.util.ArrayList<>();
    private java.util.List<String> priceRanges = new java.util.ArrayList<>();
    private String status = "ACTIVE"; // null for all (admin), "ACTIVE" for customer
    private String sortBy = "created_at"; // "price", "product_name", "created_at"
    private String sortDirection = "DESC"; // "ASC", "DESC"
    private int page = 1;
    private int pageSize = 12;

    public ProductSearchCriteria() {}

    public BigDecimal getMinDiscount() {
        return minDiscount;
    }

    public void setMinDiscount(BigDecimal minDiscount) {
        this.minDiscount = minDiscount;
        if (minDiscount != null && minDiscount.compareTo(BigDecimal.ZERO) > 0) {
            this.dealsOnly = true;
        }
    }

    public boolean isDealsOnly() {
        return dealsOnly;
    }

    public void setDealsOnly(boolean dealsOnly) {
        this.dealsOnly = dealsOnly;
    }

    public boolean hasMinDiscount(String dVal) {
        if (minDiscount == null || dVal == null) return false;
        try {
            BigDecimal target = new BigDecimal(dVal.trim());
            return minDiscount.compareTo(target) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = (categoryId != null && categoryId > 0) ? categoryId : null;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand != null && !brand.trim().isEmpty() ? brand.trim() : null;
    }

    public java.util.List<String> getBrands() {
        return brands != null ? brands : java.util.Collections.emptyList();
    }

    public void setBrands(java.util.List<String> brands) {
        this.brands = brands != null ? brands : new java.util.ArrayList<>();
    }

    public boolean hasBrand(String b) {
        if (b == null) return false;
        if (brand != null && brand.equalsIgnoreCase(b)) return true;
        if (brands != null) {
            for (String item : brands) {
                if (b.equalsIgnoreCase(item)) return true;
            }
        }
        return false;
    }

    public java.util.List<String> getPriceRanges() {
        return priceRanges != null ? priceRanges : java.util.Collections.emptyList();
    }

    public void setPriceRanges(java.util.List<String> priceRanges) {
        this.priceRanges = priceRanges != null ? priceRanges : new java.util.ArrayList<>();
    }

    public boolean hasPriceRange(String range) {
        if (range == null || priceRanges == null) return false;
        for (String r : priceRanges) {
            if (range.equalsIgnoreCase(r)) return true;
        }
        return false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        if ("price".equalsIgnoreCase(sortBy) || "product_name".equalsIgnoreCase(sortBy) || "created_at".equalsIgnoreCase(sortBy)) {
            this.sortBy = sortBy.toLowerCase();
        } else {
            this.sortBy = "created_at";
        }
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            this.sortDirection = "ASC";
        } else {
            this.sortDirection = "DESC";
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, Math.min(100, pageSize));
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
