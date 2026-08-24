package com.example.ecommerce.home.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.category.model.Category;
import com.example.ecommerce.category.service.CategoryService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.product.dto.ProductSearchCriteria;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.product.service.ProductService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Storefront Homepage.
 * Routes: / and /home
 * Aggregates Hero carousels, Deal of the Day with timer, 4-quad department boxes,
 * best sellers, dynamic category trees, and order placement success notifications.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HomeServlet.class);
    private ProductService productService;
    private CategoryService categoryService;
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 0. Detect newly placed order for logged-in user popup
            HttpSession session = request.getSession(false);
            UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
            String orderNumberParam = request.getParameter("orderNumber");

            if (user != null) {
                Order placedOrder = null;
                if (session != null && session.getAttribute("justPlacedOrder") != null) {
                    placedOrder = (Order) session.getAttribute("justPlacedOrder");
                    session.removeAttribute("justPlacedOrder");
                } else if (orderNumberParam != null && !orderNumberParam.trim().isEmpty()) {
                    try {
                        placedOrder = orderService.getOrderByNumber(orderNumberParam.trim(), user.getUserId());
                    } catch (Exception ignored) {}
                }
                if (placedOrder != null) {
                    request.setAttribute("justPlacedOrder", placedOrder);
                }
            }

            // 1. Fetch active category tree for navigation bar & mega menu
            List<Category> categoryTree = categoryService.getCategoryTree(true);
            request.setAttribute("categoryTree", categoryTree);

            // 2. Fetch full catalog slice for curated sections
            ProductSearchCriteria allCriteria = new ProductSearchCriteria();
            allCriteria.setStatus("ACTIVE");
            allCriteria.setPage(1);
            allCriteria.setPageSize(40);
            Pagination<Product> allPagination = productService.searchCatalog(allCriteria);
            List<Product> allProducts = allPagination.getItems();

            // 3. Featured Products
            List<Product> featuredProducts = allProducts.stream()
                    .limit(8)
                    .collect(Collectors.toList());
            request.setAttribute("featuredProducts", featuredProducts);

            // 4. Hot Flash Deals (Deal of the day)
            List<Product> hotDeals = allProducts.stream()
                    .filter(p -> p.getDiscountPercentage() != null && p.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0)
                    .sorted((a, b) -> b.getDiscountPercentage().compareTo(a.getDiscountPercentage()))
                    .limit(6)
                    .collect(Collectors.toList());
            request.setAttribute("hotDeals", hotDeals);

            // 5. Tech & Workstation Showcase (4 quad items)
            List<Product> techQuad = allProducts.stream()
                    .filter(p -> {
                        String name = p.getProductName().toLowerCase();
                        String brand = p.getBrand() != null ? p.getBrand().toLowerCase() : "";
                        return name.contains("macbook") || name.contains("xps") || name.contains("rog") || name.contains("galaxy") || name.contains("iphone") || brand.contains("apple") || brand.contains("dell") || brand.contains("asus") || brand.contains("samsung");
                    })
                    .limit(4)
                    .collect(Collectors.toList());
            request.setAttribute("techQuad", techQuad);

            // 6. Audio & Wearables Showcase (4 quad items)
            List<Product> audioQuad = allProducts.stream()
                    .filter(p -> {
                        String name = p.getProductName().toLowerCase();
                        return name.contains("sony") || name.contains("airpods") || name.contains("watch") || name.contains("headphone") || name.contains("audio");
                    })
                    .limit(4)
                    .collect(Collectors.toList());
            request.setAttribute("audioQuad", audioQuad);

            // 7. Fashion & Apparel Showcase (4 quad items)
            List<Product> fashionQuad = allProducts.stream()
                    .filter(p -> {
                        String name = p.getProductName().toLowerCase();
                        return name.contains("shirt") || name.contains("dress") || name.contains("jacket") || name.contains("leather") || name.contains("oxford") || name.contains("zimmermann") || name.contains("ralph");
                    })
                    .limit(4)
                    .collect(Collectors.toList());
            request.setAttribute("fashionQuad", fashionQuad);

            // 8. Home, Kitchen & Lifestyle (4 quad items)
            List<Product> lifestyleQuad = allProducts.stream()
                    .filter(p -> {
                        String name = p.getProductName().toLowerCase();
                        return name.contains("espresso") || name.contains("airfryer") || name.contains("nike") || name.contains("shoe") || name.contains("kitchen") || name.contains("coffee");
                    })
                    .limit(4)
                    .collect(Collectors.toList());
            request.setAttribute("lifestyleQuad", lifestyleQuad);

            // 9. Best Sellers / Top Rated
            List<Product> bestSellers = allProducts.stream()
                    .skip(2)
                    .limit(6)
                    .collect(Collectors.toList());
            request.setAttribute("bestSellers", bestSellers.isEmpty() ? featuredProducts : bestSellers);

            // 10. Homepage SEO Metadata
            com.example.ecommerce.seo.model.SeoMetadata seo = new com.example.ecommerce.seo.model.SeoMetadata();
            seo.setTitle("ShopKart | India's Premier Online Shopping Destination for Electronics, Mobiles & Fashion");
            seo.setDescription("ShopKart India - Discover top deals on smartphones, laptops, electronics, audio gear, and designer fashion. Enjoy free express delivery, verified reviews, and secure checkout.");
            seo.setCanonicalUrl(com.example.ecommerce.seo.model.SeoMetadata.BASE_URL + "/");
            seo.setRobots("index, follow");
            seo.setOgTitle("ShopKart | India's Premier Online Shopping Destination");
            seo.setOgDescription("Discover top deals on smartphones, laptops, audio & fashion with instant express delivery.");
            seo.setOgType("website");
            seo.setOgImage(com.example.ecommerce.seo.model.SeoMetadata.DEFAULT_IMAGE);
            seo.addBreadcrumb("Home", com.example.ecommerce.seo.model.SeoMetadata.BASE_URL + "/");
            request.setAttribute("seo", seo);

        } catch (Exception e) {
            logger.error("Error populating homepage showcase", e);
            request.setAttribute("catalogError", "Unable to load dynamic catalog items.");
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
