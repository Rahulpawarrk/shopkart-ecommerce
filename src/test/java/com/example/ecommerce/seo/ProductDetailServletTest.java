package com.example.ecommerce.seo;

import com.example.ecommerce.product.controller.ProductDetailServlet;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.review.model.RatingSummary;
import com.example.ecommerce.review.model.Review;
import com.example.ecommerce.review.service.ReviewService;
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
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDetailServlet SEO & Routing Unit Tests")
class ProductDetailServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ProductService productService;

    @Mock
    private ReviewService reviewService;

    @Test
    @DisplayName("Should issue HTTP 301 Permanent Redirect when accessed via numeric ID /product/1")
    void testNumericPathRedirects301ToSlug() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        when(request.getContextPath()).thenReturn("");

        Product prod = new Product();
        prod.setProductId(1);
        prod.setProductName("Apple MacBook Pro 16");
        prod.setSlug("apple-macbook-pro-16");
        when(productService.getProductById(1)).thenReturn(prod);

        ProductDetailServlet servlet = new ProductDetailServlet(productService, reviewService);
        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        verify(response).setHeader("Location", "/product/apple-macbook-pro-16");
    }

    @Test
    @DisplayName("Should render PDP with dynamic SeoMetadata and canonical URL when accessed via clean slug")
    void testCleanSlugRendersProductWithSeoMetadata() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/apple-macbook-pro-16");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("/index.html")).thenReturn(requestDispatcher);

        Product prod = new Product();
        prod.setProductId(1);
        prod.setProductName("Apple MacBook Pro 16");
        prod.setBrand("Apple");
        prod.setPrice(new BigDecimal("249900.00"));
        prod.setSlug("apple-macbook-pro-16");
        prod.setDescription("Top tier power laptop");
        prod.setStockQuantity(10);
        when(productService.getProductBySlug("apple-macbook-pro-16")).thenReturn(prod);

        RatingSummary rs = new RatingSummary(1, 4.8, 12);
        when(reviewService.getRatingSummary(1)).thenReturn(rs);
        when(reviewService.getProductReviews(1, 1, 20)).thenReturn(new Pagination<Review>(Collections.emptyList(), 1, 20, 0));

        ProductDetailServlet servlet = new ProductDetailServlet(productService, reviewService);
        servlet.doGet(request, response);

        ArgumentCaptor<SeoMetadata> seoCaptor = ArgumentCaptor.forClass(SeoMetadata.class);
        verify(request).setAttribute(eq("seo"), seoCaptor.capture());

        SeoMetadata capturedSeo = seoCaptor.getValue();
        assertNotNull(capturedSeo);
        assertEquals("Apple MacBook Pro 16 - Buy Online at ₹249900.00 | ShopKart", capturedSeo.getTitle());
        assertEquals("https://shopkart-ecommerce-1m2n.onrender.com/product/apple-macbook-pro-16", capturedSeo.getCanonicalUrl());
        assertEquals("product", capturedSeo.getOgType());
        verify(requestDispatcher).forward(request, response);
    }
}

