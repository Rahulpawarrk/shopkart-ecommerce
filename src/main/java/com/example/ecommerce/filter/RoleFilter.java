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
@WebFilter(filterName = "RoleFilter", urlPatterns = { "/admin", "/admin/*", "/checkout", "/checkout/*", "/cart",
        "/cart/*" })
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

        HttpSession session = httpRequest.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        String uri = httpRequest.getRequestURI();

        // 1. Admin Routes Protection (/admin/*)
        if (uri.contains("/admin")) {
            if (userSession == null || !userSession.isAdmin()) {
                logger.warn("Access denied to [{}]. User '{}' lacks ADMIN role.",
                        uri, (userSession != null ? userSession.getEmail() : "ANONYMOUS"));

                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRequest.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 2. Customer Shopping Routes Protection (/checkout/*, /cart/*)
        // Disallow logged-in ADMIN from shopping/ordering
        if (userSession != null && userSession.isAdmin()) {
            logger.info("Admin user '{}' blocked from accessing customer route: {}", userSession.getEmail(), uri);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/dashboard?error=admin_cannot_shop");
            return;
        }

        // Customer or Guest proceeding to shopping routes
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("RoleFilter destroyed.");
    }
}