package com.example.ecommerce.api.dto;

import com.example.ecommerce.util.Pagination;
import java.util.ArrayList;
import java.util.List;

public class CatalogPageResponse<T> {
    private List<T> items = new ArrayList<>();
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public CatalogPageResponse() {}

    public static <T> CatalogPageResponse<T> from(Pagination<T> p) {
        if (p == null) return new CatalogPageResponse<>();
        CatalogPageResponse<T> resp = new CatalogPageResponse<>();
        resp.setItems(p.getItems() != null ? p.getItems() : new ArrayList<>());
        resp.setPage(p.getCurrentPage());
        resp.setPageSize(p.getPageSize());
        resp.setTotalItems(p.getTotalItems());
        resp.setTotalPages(p.getTotalPages());
        resp.setHasNext(p.hasNext());
        resp.setHasPrevious(p.hasPrevious());
        return resp;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
