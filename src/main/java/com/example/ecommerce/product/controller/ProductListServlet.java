package com.example.ecommerce.product.controller;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.seo.model.SeoMetadata;
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
 * Mapped to /products, /category, and /category/* for clean SEO-friendly category routing.
 */
@WebServlet(name = "ProductListServlet", urlPatterns = {"/products", "/category", "/category/*"})
public class ProductListServlet extends HttpServlet {

    private ProductService productService;
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
    }

    public ProductListServlet(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    public ProductListServlet() {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setStatus("ACTIVE"); // Public storefront only displays active items

        Category currentCategory = null;
        boolean shouldRedirectCategory = false;
        String canonicalCategorySlug = null;

        // 1. Support clean RESTful /category/{id or slug}
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            String catSegment = pathInfo.substring(1).trim();
            if (catSegment.matches("\\d+")) {
                try {
                    int catId = Integer.parseInt(catSegment);
                    currentCategory = categoryService.getCategoryById(catId);
                    if (currentCategory != null) {
                        criteria.setCategoryId(currentCategory.getCategoryId());
                        canonicalCategorySlug = currentCategory.getSlug() != null ? currentCategory.getSlug() : String.valueOf(currentCategory.getCategoryId());
                        shouldRedirectCategory = true;
                    }
                } catch (Exception ignored) {}
            } else {
                try {
                    currentCategory = categoryService.getCategoryBySlug(catSegment);
                    if (currentCategory != null) {
                        criteria.setCategoryId(currentCategory.getCategoryId());
                        canonicalCategorySlug = currentCategory.getSlug();
                    }
                } catch (Exception e) {
                    List<Category> allCategories = categoryService.getAllCategories(true);
                    for (Category c : allCategories) {
                        if (c.getCategoryName().equalsIgnoreCase(catSegment) || 
                            c.getCategoryName().toLowerCase().replace(" ", "-").equals(catSegment.toLowerCase())) {
                            currentCategory = c;
                            criteria.setCategoryId(c.getCategoryId());
                            canonicalCategorySlug = c.getSlug() != null ? c.getSlug() : String.valueOf(c.getCategoryId());
                            break;
                        }
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

        // Auto-clean address bar: 301 redirect legacy /products?category=X to clean /category/slug
        if (pathInfo == null && categoryIdParam != null && !categoryIdParam.trim().isEmpty() && 
            (kw == null || kw.trim().isEmpty()) && request.getQueryString() != null && 
            !request.getQueryString().contains("brand") && !request.getQueryString().contains("price")) {
            try {
                int cId = Integer.parseInt(categoryIdParam.trim());
                Category c = categoryService.getCategoryById(cId);
                if (c != null) {
                    String slug = (c.getSlug() != null && !c.getSlug().trim().isEmpty()) ? c.getSlug().trim() : String.valueOf(c.getCategoryId());
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", request.getContextPath() + "/category/" + slug);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // 301 redirect numeric /category/123 to /category/{slug}
        if (shouldRedirectCategory && canonicalCategorySlug != null) {
            String target = request.getContextPath() + "/category/" + canonicalCategorySlug;
            if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
                target += "?" + request.getQueryString();
            }
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", target);
            return;
        }

        if (criteria.getCategoryId() == null && categoryIdParam != null && !categoryIdParam.trim().isEmpty()) {
            try {
                int cId = Integer.parseInt(categoryIdParam.trim());
                criteria.setCategoryId(cId);
                if (currentCategory == null) {
                    try { currentCategory = categoryService.getCategoryById(cId); } catch (Exception ignored) {}
                }
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

        // Brands Filter Selection
        String[] brandParams = request.getParameterValues("brand");
        if (brandParams != null && brandParams.length > 0) {
            java.util.List<String> bList = new java.util.ArrayList<>();
            for (String b : brandParams) {
                if (b != null && !b.trim().isEmpty() && !bList.contains(b.trim())) {
                    bList.add(b.trim());
                }
            }
            criteria.setBrands(bList);
        }

        // Sorting Option
        String sortParam = request.getParameter("sort");
        if (sortParam == null || sortParam.trim().isEmpty()) {
            sortParam = request.getParameter("sortBy");
        }
        if (sortParam != null) {
            switch (sortParam.trim()) {
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

        String pageParam = request.getParameter("page");
        int currentPage = 1;
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                currentPage = Math.max(1, Integer.parseInt(pageParam.trim()));
                criteria.setPage(currentPage);
            } catch (NumberFormatException ignored) {}
        }

        // 3. Fetch Data
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
        if (!firstEntry) jsonBuilder.append(",");
        jsonBuilder.append("\"all\":[");
        boolean firstAllB = true;
        for (String b : allBrands) {
            if (!firstAllB) jsonBuilder.append(",");
            firstAllB = false;
            jsonBuilder.append("\"").append(b.replace("\"", "\\\"")).append("\"");
        }
        jsonBuilder.append("]}");

        // 4. Build Dynamic SEO Metadata
        SeoMetadata seo = buildCatalogSeoMetadata(currentCategory, criteria, currentPage, pagination.getTotalItems());
        request.setAttribute("seo", seo);

        // 5. Bind Request Attributes
        request.setAttribute("pagination", pagination);
        request.setAttribute("criteria", criteria);
        request.setAttribute("categories", allCategories);
        request.setAttribute("categoryTree", categoryTree);
        request.setAttribute("availableBrands", availableBrands);
        request.setAttribute("allBrands", allBrands);
        request.setAttribute("categoryBrandsJson", jsonBuilder.toString());
        request.setAttribute("selectedCategory", criteria.getCategoryId());
        request.setAttribute("currentCategory", currentCategory);
        request.setAttribute("sortParam", sortParam != null ? sortParam : "newest");

        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private SeoMetadata buildCatalogSeoMetadata(Category currentCategory, ProductSearchCriteria criteria, int page, int totalCount) {
        SeoMetadata seo = new SeoMetadata();
        seo.addBreadcrumb("Home", SeoMetadata.BASE_URL + "/");

        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            // Search Results Page -> NOINDEX, FOLLOW to prevent index explosion
            String kw = criteria.getKeyword().trim();
            seo.setTitle("Search Results for \"" + kw + "\" | ShopKart");
            seo.setDescription("Explore search results for \"" + kw + "\" on ShopKart India. Enjoy top discounts, secure payments, and fast shipping.");
            seo.setRobots("noindex, follow");
            seo.setCanonicalUrl(SeoMetadata.BASE_URL + "/products");
            seo.addBreadcrumb("Search", SeoMetadata.BASE_URL + "/products");
        } else if (currentCategory != null) {
            // Category Page -> Clean Canonical & Indexable
            String catName = currentCategory.getCategoryName();
            String catSlug = currentCategory.getSlug() != null ? currentCategory.getSlug() : String.valueOf(currentCategory.getCategoryId());
            
            seo.setTitle(catName + " - Buy Online at Best Prices | ShopKart");
            
            String desc = currentCategory.getDescription();
            if (desc == null || desc.trim().isEmpty()) {
                desc = "Discover top deals on " + catName + " at ShopKart India. Free delivery, verified customer reviews, and best price guarantee.";
            }
            seo.setDescription(desc);
            
            String canonical = SeoMetadata.BASE_URL + "/category/" + catSlug;
            if (page > 1) {
                canonical += "?page=" + page;
            }
            seo.setCanonicalUrl(canonical);
            seo.setRobots("index, follow");
            seo.setOgTitle(catName + " Online Shopping | ShopKart");
            seo.setOgDescription(desc);
            seo.addBreadcrumb(catName, SeoMetadata.BASE_URL + "/category/" + catSlug);
        } else {
            // All Products Catalog
            seo.setTitle("All Products - Shop Electronics, Mobiles, Fashion & More | ShopKart");
            seo.setDescription("Browse the full catalog of products on ShopKart India. Discover top electronics, audio gear, apparel, and lifestyle accessories.");
            String canonical = SeoMetadata.BASE_URL + "/products";
            if (page > 1) {
                canonical += "?page=" + page;
            }
            seo.setCanonicalUrl(canonical);
            seo.setRobots("index, follow");
            seo.addBreadcrumb("All Products", SeoMetadata.BASE_URL + "/products");
        }

        return seo;
    }
}

