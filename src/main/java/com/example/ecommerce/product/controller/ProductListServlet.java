package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller handling public product catalog browsing, filtering, search, sorting, and pagination.
 * Mapped to /products.
 */
@WebServlet(name = "ProductListServlet", urlPatterns = {"/products", "/category/*"})
public class ProductListServlet extends HttpServlet {

    private ProductService productService;
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE"); // Public storefront only displays active items

        // 1. Support clean RESTful /category/{id or slug}
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String catSegment = pathInfo.substring(1).trim();
            try {
                int catId = Integer.parseInt(catSegment);
                criteria.setCategoryId(catId);
            } catch (NumberFormatException e) {
                List<Category> allCategories = categoryService.getAllCategories(true);
                for (Category c : allCategories) {
                    if (c.getCategoryName().equalsIgnoreCase(catSegment) || 
                        c.getCategoryName().toLowerCase().replace(" ", "-").equals(catSegment.toLowerCase())) {
                        criteria.setCategoryId(c.getCategoryId());
                        break;
                    }
                }
            }
        }

        // 2. Parse Query Parameters
        String kw = request.getParameter("q");
        if (kw == null || kw.trim().isEmpty()) {
            kw = request.getParameter("keyword");
        }
        criteria.setKeyword(kw);
        
        String categoryIdParam = request.getParameter("category");
        if (categoryIdParam == null || categoryIdParam.trim().isEmpty()) {
            categoryIdParam = request.getParameter("categoryId");
        }

        // Auto-clean address bar: redirect /products?category=X to clean /category/X
        if (pathInfo == null && categoryIdParam != null && !categoryIdParam.trim().isEmpty() && (kw == null || kw.trim().isEmpty()) && request.getQueryString() != null && !request.getQueryString().contains("brand") && !request.getQueryString().contains("price")) {
            response.sendRedirect(request.getContextPath() + "/category/" + categoryIdParam.trim());
            return;
        }

        if (categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
            try {
                criteria.setCategoryId(Integer.parseInt(categoryIdParam.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String minPriceParam = request.getParameter("minPrice");
        if (minPriceParam != null && !minPriceParam.trim().isEmpty()) {
            try {
                criteria.setMinPrice(new BigDecimal(minPriceParam.trim()));
            } catch (Exception ignored) {}
        }

        String maxPriceParam = request.getParameter("maxPrice");
        if (maxPriceParam != null && !maxPriceParam.trim().isEmpty()) {
            try {
                criteria.setMaxPrice(new BigDecimal(maxPriceParam.trim()));
            } catch (Exception ignored) {}
        }

        // Price Ranges Multiple Selection
        String[] priceRanges = request.getParameterValues("priceRange");
        if (priceRanges != null && priceRanges.length > 0) {
            java.util.List<String> list = new java.util.ArrayList<>();
            for (String pr : priceRanges) {
                if (pr != null && !pr.trim().isEmpty()) {
                    list.add(pr.trim());
                }
            }
            criteria.setPriceRanges(list);
        }

        // Brands Filter Selection (Support both multiple or single brand params)
        String[] brandParams = request.getParameterValues("brand");
        if (brandParams != null && brandParams.length > 0) {
            java.util.List<String> bList = new java.util.ArrayList<>();
            for (String b : brandParams) {
                if (b != null && !b.trim().isEmpty() && !bList.contains(b.trim())) {
                    bList.add(b.trim());
                }
            }
            criteria.setBrands(bList);
            if (bList.size() == 1) {
                criteria.setBrand(bList.get(0));
            }
        }

        // Deals & Discount Filter
        String dealsParam = request.getParameter("deals");
        if (dealsParam == null || dealsParam.trim().isEmpty()) {
            dealsParam = request.getParameter("deal");
        }
        if ("true".equalsIgnoreCase(dealsParam) || "1".equals(dealsParam)) {
            criteria.setDealsOnly(true);
        }

        String minDiscountParam = request.getParameter("minDiscount");
        if (minDiscountParam != null && !minDiscountParam.trim().isEmpty()) {
            try {
                criteria.setMinDiscount(new BigDecimal(minDiscountParam.trim()));
            } catch (Exception ignored) {}
        }

        String sortParam = request.getParameter("sort");
        if (sortParam == null || sortParam.trim().isEmpty()) {
            sortParam = request.getParameter("sortBy");
        }
        if (sortParam != null) {
            switch (sortParam) {
                case "price_asc", "price_low", "price" -> {
                    criteria.setSortBy("price");
                    criteria.setSortDirection("ASC");
                }
                case "price_desc", "price_high" -> {
                    criteria.setSortBy("price");
                    criteria.setSortDirection("DESC");
                }
                case "name_asc", "product_name" -> {
                    criteria.setSortBy("product_name");
                    criteria.setSortDirection("ASC");
                }
                case "discount_desc", "discount", "deals" -> {
                    criteria.setSortBy("discount_percentage");
                    criteria.setSortDirection("DESC");
                }
                default -> {
                    criteria.setSortBy("created_at");
                    criteria.setSortDirection("DESC");
                }
            }
        }

        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                criteria.setPage(Integer.parseInt(pageParam.trim()));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Fetch Data
        Pagination<Product> pagination = productService.searchCatalog(criteria);
        List<Category> allCategories = categoryService.getAllCategories(true);
        List<Category> categoryTree = categoryService.getCategoryTree(true);
        List<String> availableBrands = productService.getDistinctBrands(criteria.getCategoryId());
        java.util.Map<Integer, List<String>> categoryBrandsMap = productService.getCategoryBrandsMap();
        List<String> allBrands = productService.getDistinctBrands(null);

        // Build simple JSON for category -> brand mapping for client-side reactivity
        StringBuilder jsonBuilder = new StringBuilder("{");
        boolean firstEntry = true;
        for (java.util.Map.Entry<Integer, List<String>> entry : categoryBrandsMap.entrySet()) {
            if (!firstEntry) jsonBuilder.append(",");
            firstEntry = false;
            jsonBuilder.append("\"").append(entry.getKey()).append("\":[");
            boolean firstB = true;
            for (String b : entry.getValue()) {
                if (!firstB) jsonBuilder.append(",");
                firstB = false;
                jsonBuilder.append("\"").append(b.replace("\"", "\\\"")).append("\"");
            }
            jsonBuilder.append("]");
        }
        // Include "all" key for root
        if (!firstEntry) jsonBuilder.append(",");
        jsonBuilder.append("\"all\":[");
        boolean firstAllB = true;
        for (String b : allBrands) {
            if (!firstAllB) jsonBuilder.append(",");
            firstAllB = false;
            jsonBuilder.append("\"").append(b.replace("\"", "\\\"")).append("\"");
        }
        jsonBuilder.append("]}");

        // 3. Bind Request Attributes
        request.setAttribute("pagination", pagination);
        request.setAttribute("criteria", criteria);
        request.setAttribute("categories", allCategories);
        request.setAttribute("categoryTree", categoryTree);
        request.setAttribute("availableBrands", availableBrands);
        request.setAttribute("allBrands", allBrands);
        request.setAttribute("categoryBrandsJson", jsonBuilder.toString());
        request.setAttribute("selectedCategory", criteria.getCategoryId());
        request.setAttribute("sortParam", sortParam != null ? sortParam : "newest");

        request.getRequestDispatcher("/WEB-INF/views/product/list.jsp").forward(request, response);
    }
}
