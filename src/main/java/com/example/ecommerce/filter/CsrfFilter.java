package com.example.ecommerce.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Cross-Site Request Forgery (CSRF) Protection Filter.
 * Generates cryptographic session tokens and enforces CSRF validation on
 * state-changing requests.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);
    private static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    private static final String CSRF_REQ_ATTR = "csrfToken";
    private static final SecureRandom secureRandom = new SecureRandom();

    // Paths exempted from CSRF (External Webhooks / Payment Gateways / Authentication APIs)
    private static final Set<String> EXEMPT_PREFIXES = Set.of(
            "/api/auth",
            "/auth",
            "/login",
            "/register",
            "/api/logistics/webhook",
            "/payment/callback",
            "/api/payment/webhook");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CsrfFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(true);
        String sessionCsrfToken = (String) session.getAttribute(CSRF_SESSION_ATTR);

        if (sessionCsrfToken == null || sessionCsrfToken.isEmpty()) {
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            sessionCsrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            session.setAttribute(CSRF_SESSION_ATTR, sessionCsrfToken);
        }

        // Expose token across session and request attributes for JSPs
        session.setAttribute("csrfToken", sessionCsrfToken);
        httpRequest.setAttribute(CSRF_REQ_ATTR, sessionCsrfToken);
        httpRequest.setAttribute("_csrf", sessionCsrfToken);

        // Expose non-HttpOnly XSRF-TOKEN cookie so frontend JavaScript can read it
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", sessionCsrfToken);
        xsrfCookie.setPath(httpRequest.getContextPath().isEmpty() ? "/" : httpRequest.getContextPath());
        xsrfCookie.setHttpOnly(false);
        xsrfCookie.setSecure(httpRequest.isSecure());
        httpResponse.addCookie(xsrfCookie);

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Only enforce CSRF on mutating operations (POST, PUT, DELETE, PATCH)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)) {

            // Check if path is exempted (e.g. carrier webhooks, gateway callback)
            boolean isExempt = false;
            for (String prefix : EXEMPT_PREFIXES) {
                if (path.startsWith(prefix)) {
                    isExempt = true;
                    break;
                }
            }

            if (!isExempt) {
                String reqCsrfToken = httpRequest.getParameter("_csrf");
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getParameter("csrfToken");
                }
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getParameter("csrf_token");
                }
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getHeader("X-CSRF-Token");
                }
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getHeader("X-XSRF-TOKEN");
                }
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getHeader("X-CSRF-TOKEN");
                }

                boolean valid = false;
                if (reqCsrfToken != null && !reqCsrfToken.trim().isEmpty()) {
                    byte[] reqBytes = reqCsrfToken.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    byte[] sessionBytes = sessionCsrfToken.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    valid = java.security.MessageDigest.isEqual(reqBytes, sessionBytes);
                }

                if (!valid) {
                    logger.warn("CSRF validation blocked request to [{}] from IP [{}] (Method: {})", path,
                            httpRequest.getRemoteAddr(), method);
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("CsrfFilter destroyed.");
    }
}
