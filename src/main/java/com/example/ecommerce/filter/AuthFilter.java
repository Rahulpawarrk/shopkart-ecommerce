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
 * Filter that intercepts protected routes and ensures an authenticated UserSession exists.
 * Mapped to customer-protected routes and admin paths.
 *
 * Explicitly NOT mapped (public routes):
 *   /login, /logout, /register, /forgot-password, /reset-password, /products, /health
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/profile", "/profile/*",
        "/change-password",
        "/cart", "/cart/*",
        "/wishlist", "/wishlist/*",
        "/checkout", "/checkout/*",
        "/orders", "/orders/*",
        "/addresses", "/addresses/*",
        "/product/review",
        "/payment", "/payment/*",
        "/admin", "/admin/*"
})
public class AuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (userSession == null) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String redirectTarget = requestURI + (queryString != null ? "?" + queryString : "");

            logger.debug("Unauthenticated access attempt to [{}]. Redirecting to /login", redirectTarget);
            
            // Save redirect target to return user to requested page after login
            if (session == null) {
                session = httpRequest.getSession(true);
            }
            session.setAttribute("redirectAfterLogin", redirectTarget);

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?error=auth_required");
            return;
        }

        // Authenticated, proceed down filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed.");
    }
}
