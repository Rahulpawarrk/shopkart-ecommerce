package com.example.ecommerce.seo;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.seo.controller.SitemapServlet;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SitemapServlet Unit Tests")
class SitemapServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    @Test
    @DisplayName("Should generate valid Sitemaps.org XML containing homepage, catalog, categories, and products")
    void testDoGetSitemapXml() throws ServletException, IOException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        Category cat = new Category(1, null, "Laptops & Computers", "laptops-computers", "Laptops desc", true);
        cat.setUpdatedAt(LocalDateTime.of(2026, 8, 20, 10, 0));
        when(categoryService.getAllCategories(true)).thenReturn(List.of(cat));

        Product prod = new Product();
        prod.setProductId(1);
        prod.setProductName("Apple MacBook Pro 16");
        prod.setSlug("apple-macbook-pro-16");
        prod.setUpdatedAt(LocalDateTime.of(2026, 8, 22, 12, 30));
        Pagination<Product> productPagination = new Pagination<>(List.of(prod), 1, 1, 1);
        when(productService.searchCatalog(any(ProductSearchCriteria.class))).thenReturn(productPagination);

        SitemapServlet servlet = new SitemapServlet(categoryService, productService);
        servlet.doGet(request, response);

        verify(response).setContentType("application/xml; charset=UTF-8");
        verify(response).setHeader("Cache-Control", "public, max-age=3600");

        String output = stringWriter.toString();
        assertTrue(output.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(output.contains("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\""));
        assertTrue(output.contains("<loc>https://shopkart-ecommerce-1m2n.onrender.com/</loc>"));
        assertTrue(output.contains("<loc>https://shopkart-ecommerce-1m2n.onrender.com/products</loc>"));
        assertTrue(output.contains("<loc>https://shopkart-ecommerce-1m2n.onrender.com/category/laptops-computers</loc>"));
        assertTrue(output.contains("<loc>https://shopkart-ecommerce-1m2n.onrender.com/product/apple-macbook-pro-16</loc>"));
        assertTrue(output.contains("<lastmod>2026-08-22</lastmod>"));
    }
}
