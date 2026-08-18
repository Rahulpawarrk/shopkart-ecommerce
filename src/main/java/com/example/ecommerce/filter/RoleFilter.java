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
 * Role-Based Access Control (RBAC) Filter for administrative routes.
 * Intercepts /admin and /admin/* to verify the user holds the 'ADMIN' role.
 */
@WebFilter(filterName = "RoleFilter", urlPatterns = {"/admin", "/admin/*"})
public class RoleFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RoleFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RoleFilter (Admin RBAC) initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (userSession == null || !userSession.isAdmin()) {
            logger.warn("Access denied to [{}]. User '{}' lacks ADMIN role.", 
                    httpRequest.getRequestURI(), 
                    (userSession != null ? userSession.getEmail() : "ANONYMOUS"));
            
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            httpRequest.getRequestDispatcher("/WEB-INF/views/error/403.jsp").forward(request, response);
            return;
        }

        // User is authorized as ADMIN
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("RoleFilter destroyed.");
    }
}
