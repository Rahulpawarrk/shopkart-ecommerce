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
    private boolean enforceHttps = true; // Enabled by default for production security

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String enforceEnv = System.getenv("ENFORCE_HTTPS");
        String enforceProp = System.getProperty("ecommerce.enforce.https");
        if ("false".equalsIgnoreCase(enforceEnv) || "false".equalsIgnoreCase(enforceProp)) {
            this.enforceHttps = false;
        } else {
            this.enforceHttps = true;
        }
        logger.info("SecurityHeadersFilter initialized (enforceHttps={}).", enforceHttps);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            String host = httpRequest.getHeader("Host");
            if (host == null || host.trim().isEmpty()) {
                host = httpRequest.getServerName();
            }

            boolean isLocalhost = host != null && (host.startsWith("localhost") || host.startsWith("127.0.0.1") || host.startsWith("[::1]"));
            String forwardedProto = httpRequest.getHeader("X-Forwarded-Proto");
            boolean isHttps = httpRequest.isSecure() || "https".equalsIgnoreCase(forwardedProto);

            // 0. Enforce HTTP -> HTTPS 301 Redirect on production domains or when enforceHttps is active (skipping plain local dev unless explicitly enabled)
            if (enforceHttps && !isHttps && !isLocalhost) {
                String targetUrl = "https://" + host + httpRequest.getRequestURI();
                if (httpRequest.getQueryString() != null) {
                    targetUrl += "?" + httpRequest.getQueryString();
                }
                httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpResponse.setHeader("Location", targetUrl);
                return;
            }

            // 1. Prevent MIME-type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // 2. Clickjacking protection (allow same origin framing only)
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

            // 3. XSS Filter protection for legacy browsers
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // 4. Referrer Policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Generate a cryptographically secure random nonce per request for CSP
            byte[] nonceBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(nonceBytes);
            String cspNonce = java.util.Base64.getEncoder().encodeToString(nonceBytes);
            httpRequest.setAttribute("cspNonce", cspNonce);

            // 5. Content Security Policy (CSP) - Hardened OWASP & Mozilla Observatory Compliant Policy
            httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'nonce-" + cspNonce + "' https://checkout.razorpay.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com data:; " +
                    "img-src 'self' data: https: blob:; " +
                    "connect-src 'self' https://api.razorpay.com https://lumberjack.razorpay.com; " +
                    "frame-src 'self' https://api.razorpay.com https://checkout.razorpay.com; " +
                    "object-src 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'; " +
                    "frame-ancestors 'self'; " +
                    "upgrade-insecure-requests;");

            // 6. Permissions Policy (restricting sensitive browser features & APIs)
            httpResponse.setHeader("Permissions-Policy",
                    "accelerometer=(), " +
                    "autoplay=(), " +
                    "camera=(), " +
                    "display-capture=(), " +
                    "encrypted-media=(), " +
                    "fullscreen=(self), " +
                    "geolocation=(), " +
                    "gyroscope=(), " +
                    "magnetometer=(), " +
                    "microphone=(), " +
                    "midi=(), " +
                    "payment=(self), " +
                    "usb=()");

            // 7. Cross-Origin Opener Policy
            httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");

            // 8. Enforce HTTPS via HSTS if request is secure
            if (isHttps) {
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
