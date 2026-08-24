package com.example.ecommerce.seo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Serves production-grade /robots.txt directives to search engine crawlers (Googlebot, Bingbot, etc.).
 * Allows public catalog and product pages while blocking private, authenticated, and administrative endpoints.
 */
@WebServlet(name = "RobotsServlet", urlPatterns = {"/robots.txt"})
public class RobotsServlet extends HttpServlet {

    private static final String SITEMAP_URL = "https://shopkart-ecommerce-1m2n.onrender.com/sitemap.xml";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // Cache robots.txt for 24 hours
        response.setHeader("Cache-Control", "public, max-age=86400");

        try (PrintWriter out = response.getWriter()) {
            out.println("# ==============================================================================");
            out.println("# ShopKart E-Commerce Robots Directives");
            out.println("# Production URL: https://shopkart-ecommerce-1m2n.onrender.com");
            out.println("# ==============================================================================");
            out.println();
            out.println("User-agent: *");
            out.println();
            out.println("# Public Crawlable Storefront Routes");
            out.println("Allow: /");
            out.println("Allow: /home");
            out.println("Allow: /products");
            out.println("Allow: /category/");
            out.println("Allow: /product/");
            out.println("Allow: /assets/");
            out.println("Allow: /favicon.svg");
            out.println("Allow: /robots.txt");
            out.println("Allow: /sitemap.xml");
            out.println();
            out.println("# Disallow Private, Authenticated, and Internal Endpoints");
            out.println("Disallow: /admin/");
            out.println("Disallow: /cart");
            out.println("Disallow: /cart/");
            out.println("Disallow: /checkout");
            out.println("Disallow: /checkout/");
            out.println("Disallow: /payment/");
            out.println("Disallow: /profile");
            out.println("Disallow: /orders");
            out.println("Disallow: /wishlist");
            out.println("Disallow: /addresses");
            out.println("Disallow: /auth/");
            out.println("Disallow: /login");
            out.println("Disallow: /register");
            out.println("Disallow: /forgot-password");
            out.println("Disallow: /reset-password");
            out.println("Disallow: /verify-email");
            out.println("Disallow: /change-password");
            out.println("Disallow: /health");
            out.println("Disallow: /search-suggestions");
            out.println("Disallow: /logistics/");
            out.println();
            out.println("# Sitemap reference");
            out.println("Sitemap: " + SITEMAP_URL);
        }
    }
}
