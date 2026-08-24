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
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * Cross-Site Request Forgery (CSRF) Protection Filter.
 * Generates cryptographic session tokens and enforces CSRF validation on state-changing requests.
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);
    private static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    private static final String CSRF_REQ_ATTR = "csrfToken";
    private static final SecureRandom secureRandom = new SecureRandom();

    // Paths exempted from CSRF (External Webhooks / Payment Gateways)
    private static final Set<String> EXEMPT_PREFIXES = Set.of(
            "/api/logistics/webhook",
            "/payment/callback"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CsrfFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
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

        httpRequest.setAttribute(CSRF_REQ_ATTR, sessionCsrfToken);

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Only enforce CSRF on mutating operations (POST, PUT, DELETE, PATCH)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {

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
                    reqCsrfToken = httpRequest.getHeader("X-CSRF-Token");
                }
                if (reqCsrfToken == null || reqCsrfToken.isEmpty()) {
                    reqCsrfToken = httpRequest.getHeader("X-XSRF-TOKEN");
                }

                UserSession currentUser = (UserSession) session.getAttribute("currentUser");

                // Enforce strictly for authenticated users or state-changing operations
                if (currentUser != null && (reqCsrfToken == null || !reqCsrfToken.equals(sessionCsrfToken))) {
                    // Check Origin / Referer header validation as secondary defense
                    String origin = httpRequest.getHeader("Origin");
                    String referer = httpRequest.getHeader("Referer");
                    boolean isSameOrigin = isAllowedOrigin(httpRequest, origin) || isAllowedOrigin(httpRequest, referer);

                    if (!isSameOrigin && (reqCsrfToken == null || !reqCsrfToken.equals(sessionCsrfToken))) {
                        logger.warn("CSRF validation blocked request to [{}] from IP [{}] (Method: {})", path, httpRequest.getRemoteAddr(), method);
                        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("CsrfFilter destroyed.");
    }

    private boolean isAllowedOrigin(HttpServletRequest request, String header) {
        if (header == null || header.isBlank()) return false;
        try {
            java.net.URI uri = new java.net.URI(header);
            String host = uri.getHost();
            if (host == null) return false;

            String serverName = request.getServerName();
            if (host.equalsIgnoreCase(serverName)) return true;

            String hostHeader = request.getHeader("Host");
            if (hostHeader != null) {
                String cleanHost = hostHeader.contains(":") ? hostHeader.split(":")[0] : hostHeader;
                if (host.equalsIgnoreCase(cleanHost)) return true;
            }

            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (forwardedHost != null) {
                String cleanForwardedHost = forwardedHost.contains(":") ? forwardedHost.split(":")[0] : forwardedHost;
                if (host.equalsIgnoreCase(cleanForwardedHost)) return true;
            }

            return false;
        } catch (java.net.URISyntaxException e) {
            return false;
        }
    }
}
