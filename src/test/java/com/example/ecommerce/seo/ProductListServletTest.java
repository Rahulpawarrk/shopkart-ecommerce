package com.example.ecommerce.seo;

import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.product.controller.ProductListServlet;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.seo.model.SeoMetadata;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductListServlet SEO & Routing Unit Tests")
class ProductListServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Test
    @DisplayName("Should issue HTTP 301 Permanent Redirect for legacy /products?category=1 to clean /category/slug")
    void testLegacyCategoryParamRedirects301() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        when(request.getParameter("category")).thenReturn("1");
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getQueryString()).thenReturn("category=1");
        when(request.getContextPath()).thenReturn("");

        Category cat = new Category(1, null, "Laptops & Computers", "laptops-computers", "Laptops desc", true);
        when(categoryService.getCategoryById(1)).thenReturn(cat);

        ProductListServlet servlet = new ProductListServlet(productService, categoryService);
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", "/category/laptops-computers");
    }

    @Test
    @DisplayName("Should set noindex, follow for Search results to prevent crawl bloat")
    void testSearchQuerySetsNoindexFollow() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        when(request.getParameter("q")).thenReturn("macbook");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/WEB-INF/views/product/list.jsp")).thenReturn(requestDispatcher);

        when(productService.searchCatalog(any(ProductSearchCriteria.class))).thenReturn(new Pagination<>(Collections.emptyList(), 1, 12, 0));
        when(categoryService.getAllCategories(true)).thenReturn(List.of());
        when(categoryService.getCategoryTree(true)).thenReturn(List.of());
        when(productService.getCategoryBrandsMap()).thenReturn(Collections.emptyMap());

        ProductListServlet servlet = new ProductListServlet(productService, categoryService);
        servlet.doGet(request, response);

        ArgumentCaptor<SeoMetadata> seoCaptor = ArgumentCaptor.forClass(SeoMetadata.class);
        verify(request).setAttribute(eq("seo"), seoCaptor.capture());

        SeoMetadata capturedSeo = seoCaptor.getValue();
        assertNotNull(capturedSeo);
        assertEquals("noindex, follow", capturedSeo.getRobots());
        assertEquals("Search Results for \"macbook\" | ShopKart", capturedSeo.getTitle());
        assertEquals("https://shopkart-ecommerce-1m2n.onrender.com/products", capturedSeo.getCanonicalUrl());
    }
}
