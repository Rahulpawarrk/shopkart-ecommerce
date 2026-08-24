package com.example.ecommerce.seo.controller;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dynamically serves /sitemap.xml conforming to the official Sitemaps.org XML schema.
 * Aggregates public storefront homepage, catalog, active categories, and active products.
 */
@WebServlet(name = "SitemapServlet", urlPatterns = {"/sitemap.xml"})
public class SitemapServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SitemapServlet.class);
    private static final String BASE_URL = "https://shopkart-ecommerce-1m2n.onrender.com";
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CategoryService categoryService;
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.categoryService = new CategoryService();
        this.productService = new ProductService();
    }

    // Constructor for testing with mocked services
    public SitemapServlet(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    public SitemapServlet() {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/xml; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // Cache sitemap for 1 hour
        response.setHeader("Cache-Control", "public, max-age=3600");

        try (PrintWriter out = response.getWriter()) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"");
            out.println("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            out.println("        xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">");

            String todayStr = LocalDateTime.now().format(ISO_DATE_FORMATTER);

            // 1. Homepage
            writeUrlElement(out, BASE_URL + "/", todayStr, "daily", "1.0");

            // 2. Main Products Catalog
            writeUrlElement(out, BASE_URL + "/products", todayStr, "daily", "0.8");

            // 3. Active Categories
            try {
                List<Category> categories = categoryService.getAllCategories(true);
                if (categories != null) {
                    for (Category cat : categories) {
                        String catSlug = (cat.getSlug() != null && !cat.getSlug().trim().isEmpty()) 
                                ? cat.getSlug().trim() 
                                : String.valueOf(cat.getCategoryId());
                        String catUrl = BASE_URL + "/category/" + catSlug;
                        String lastmod = cat.getUpdatedAt() != null 
                                ? cat.getUpdatedAt().format(ISO_DATE_FORMATTER) 
                                : (cat.getCreatedAt() != null ? cat.getCreatedAt().format(ISO_DATE_FORMATTER) : todayStr);
                        writeUrlElement(out, catUrl, lastmod, "weekly", "0.8");
                    }
                }
            } catch (Exception e) {
                logger.error("Error generating category sitemap nodes", e);
            }

            // 4. Active Products
            try {
                ProductSearchCriteria criteria = new ProductSearchCriteria();
                criteria.setStatus("ACTIVE");
                criteria.setPage(1);
                criteria.setPageSize(1000); // Export top 1000 active products in sitemap
                Pagination<Product> productPage = productService.searchCatalog(criteria);
                if (productPage != null && productPage.getItems() != null) {
                    for (Product prod : productPage.getItems()) {
                        String prodSlug = (prod.getSlug() != null && !prod.getSlug().trim().isEmpty()) 
                                ? prod.getSlug().trim() 
                                : String.valueOf(prod.getProductId());
                        String prodUrl = BASE_URL + "/product/" + prodSlug;
                        String lastmod = prod.getUpdatedAt() != null 
                                ? prod.getUpdatedAt().format(ISO_DATE_FORMATTER) 
                                : (prod.getCreatedAt() != null ? prod.getCreatedAt().format(ISO_DATE_FORMATTER) : todayStr);
                        writeUrlElement(out, prodUrl, lastmod, "daily", "0.9");
                    }
                }
            } catch (Exception e) {
                logger.error("Error generating product sitemap nodes", e);
            }

            out.println("</urlset>");
        }
    }

    private void writeUrlElement(PrintWriter out, String loc, String lastmod, String changefreq, String priority) {
        out.println("  <url>");
        out.println("    <loc>" + escapeXml(loc) + "</loc>");
        if (lastmod != null && !lastmod.isEmpty()) {
            out.println("    <lastmod>" + escapeXml(lastmod) + "</lastmod>");
        }
        if (changefreq != null && !changefreq.isEmpty()) {
            out.println("    <changefreq>" + escapeXml(changefreq) + "</changefreq>");
        }
        if (priority != null && !priority.isEmpty()) {
            out.println("    <priority>" + escapeXml(priority) + "</priority>");
        }
        out.println("  </url>");
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
