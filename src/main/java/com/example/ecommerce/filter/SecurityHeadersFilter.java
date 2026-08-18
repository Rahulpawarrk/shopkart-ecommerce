package com.example.ecommerce.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Production Security Headers Filter.
 * Applies OWASP recommended security response headers across all application endpoints.
 */
@WebFilter(filterName = "SecurityHeadersFilter", urlPatterns = "/*")
public class SecurityHeadersFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityHeadersFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("SecurityHeadersFilter initialized for production defense-in-depth.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 1. Prevent MIME-type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // 2. Clickjacking protection (allow same origin framing only)
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

            // 3. XSS Filter protection for legacy browsers
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // 4. Referrer Policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // 5. Enforce HTTPS via HSTS if request is secure
            if (httpRequest.isSecure() || "https".equalsIgnoreCase(httpRequest.getHeader("X-Forwarded-Proto"))) {
                httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("SecurityHeadersFilter destroyed.");
    }
}
