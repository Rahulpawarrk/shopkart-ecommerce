package com.example.ecommerce.product.controller;

import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Lightweight JSON API providing real-time live search suggestions for the global search bar.
 */
@WebServlet(name = "SearchSuggestionServlet", urlPatterns = {"/api/products/search"})
public class SearchSuggestionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchSuggestionServlet.class);
    private final ProductService productService;

    public SearchSuggestionServlet() {
        this.productService = new ProductService();
    }

    public SearchSuggestionServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        String query = request.getParameter("q");
        String catIdParam = request.getParameter("category");

        if (query == null || query.trim().length() < 1) {
            try (PrintWriter out = response.getWriter()) {
                out.print("[]");
            }
            return;
        }

        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria();
            criteria.setKeyword(query.trim());
            if (catIdParam != null && !catIdParam.trim().isEmpty()) {
                try {
                    int catId = Integer.parseInt(catIdParam.trim());
                    if (catId > 0) {
                        criteria.setCategoryId(catId);
                    }
                } catch (NumberFormatException ignored) {}
            }
            criteria.setStatus("ACTIVE");
            criteria.setPage(1);
            criteria.setPageSize(6);

            Pagination<Product> result = productService.searchCatalog(criteria);
            List<Product> products = result.getItems();

            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"id\":").append(p.getProductId()).append(",")
                    .append("\"name\":\"").append(escapeJson(p.getProductName())).append("\",")
                    .append("\"brand\":\"").append(escapeJson(p.getBrand() != null ? p.getBrand() : "")).append("\",")
                    .append("\"category\":\"").append(escapeJson(p.getCategory() != null ? p.getCategory().getCategoryName() : "")).append("\",")
                    .append("\"price\":").append(p.getPrice()).append(",")
                    .append("\"effectivePrice\":").append(p.getEffectivePrice()).append(",")
                    .append("\"discount\":").append(p.getDiscountPercentage()).append(",")
                    .append("\"image\":\"").append(escapeJson(p.getPrimaryImageUrl() != null ? p.getPrimaryImageUrl() : "")).append("\"")
                    .append("}");
            }
            json.append("]");

            try (PrintWriter out = response.getWriter()) {
                out.print(json.toString());
            }

        } catch (Exception e) {
            logger.error("Error retrieving search suggestions for query: {}", query, e);
            try (PrintWriter out = response.getWriter()) {
                out.print("[]");
            }
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
