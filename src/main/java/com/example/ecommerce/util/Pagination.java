package com.example.ecommerce.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic container for paginated query results.
 *
 * @param <T> the type of elements in the page
 */
public class Pagination<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;

    public Pagination(List<T> items, int currentPage, int pageSize, int totalItems) {
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.currentPage = Math.max(1, currentPage);
        this.pageSize = Math.max(1, pageSize);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }
}
