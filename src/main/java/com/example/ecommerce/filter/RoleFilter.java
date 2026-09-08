package com.example.ecommerce.filter;

import com.example.ecommerce.auth.model.UserSession;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Role-Based Access Control (RBAC) Filter.
 * 1. Restricts /admin and /admin/* strictly to users with the 'ADMIN' role.
 * 2. Restricts /checkout, /cart, and customer /orders away from 'ADMIN' users.
 */
@WebFilter(filterName = "RoleFilter", urlPatterns = {
        "/admin", "/admin/*",
        "/checkout", "/checkout/*",
        "/cart", "/cart/*",
        "/addresses", "/addresses/*",
        "/wishlist", "/wishlist/*",
        "/orders", "/orders/*",
        "/order", "/order/*",
        "/product/review", "/order/review",
        "/api/admin", "/api/admin/*",
        "/api/cart", "/api/cart/*",
        "/api/orders", "/api/orders/*",
        "/api/customer", "/api/customer/*",
        "/api/wishlist", "/api/wishlist/*"
})
public class RoleFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RoleFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RoleFilter (RBAC) initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        String uri = httpRequest.getRequestURI();
        String path = httpRequest.getServletPath();

        boolean isApi = uri.contains("/api/") ||
                "XMLHttpRequest".equalsIgnoreCase(httpRequest.getHeader("X-Requested-With")) ||
                (httpRequest.getHeader("Accept") != null && httpRequest.getHeader("Accept").contains("application/json"));

        // 1. Admin Routes Protection (/admin/* or /api/admin/*)
        if (path.startsWith("/admin") || path.startsWith("/api/admin")) {
            if (userSession == null || !userSession.isAdmin()) {
                logger.warn("Access denied to [{}]. User '{}' lacks ADMIN role.",
                        uri, (userSession != null ? userSession.getEmail() : "ANONYMOUS"));

                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                if (isApi) {
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"success\":false,\"message\":\"Access denied. Admin privileges required.\",\"code\":\"FORBIDDEN\"}");
                    return;
                }
                httpRequest.getRequestDispatcher("/index.html").forward(request, response);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 2. Customer Modules Protection (Admin cannot access customer shopping, cart, wishlist, addresses, or place orders)
        if (userSession != null && userSession.isAdmin()) {
            logger.info("Admin user '{}' blocked from accessing customer module: {}", userSession.getEmail(), uri);

            if (isApi) {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("{\"success\":false,\"message\":\"Admin accounts are restricted from customer shopping, cart, wishlist, and address modules.\",\"code\":\"FORBIDDEN\"}");
                return;
            }

            if (path.startsWith("/orders") || path.startsWith("/order")) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/orders");
                return;
            }

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/dashboard?error=admin_restricted");
            return;
        }

        // Customer or Guest proceeding to customer routes
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("RoleFilter destroyed.");
    }
}
