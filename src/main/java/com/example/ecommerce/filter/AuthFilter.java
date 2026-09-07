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
 * Authentication filter safeguarding protected customer & admin endpoints.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/account", "/account/*",
        "/profile", "/profile/*",
        "/change-password", "/change-password/*",
        "/addresses", "/addresses/*",
        "/orders", "/orders/*",
        "/order", "/order/*",
        "/returns", "/returns/*",
        "/order/return",
        "/order/confirmation", "/order/confirmed",
        "/payment", "/payment/*",
        "/product/review", "/order/review",
        "/checkout", "/checkout/*",
        "/cart", "/cart/*",
        "/wishlist", "/wishlist/*",
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
            String targetUrl = httpRequest.getRequestURI();
            String query = httpRequest.getQueryString();
            if (query != null && !query.isEmpty()) {
                targetUrl += "?" + query;
            }

            session = httpRequest.getSession(true);

            // Capture direct buy product & quantity if present in request (GET query or POST body)
            String buyNowPid = httpRequest.getParameter("buyNowProductId");
            if (buyNowPid != null && !buyNowPid.trim().isEmpty()) {
                try {
                    int pid = Integer.parseInt(buyNowPid.trim());
                    int qty = 1;
                    String qtyParam = httpRequest.getParameter("quantity");
                    if (qtyParam != null && !qtyParam.trim().isEmpty()) {
                        try {
                            qty = Integer.parseInt(qtyParam.trim());
                        } catch (NumberFormatException ignored) {}
                    }
                    if (qty <= 0) qty = 1;
                    if (qty > 3) qty = 3;

                    session.setAttribute("directBuyProductId", pid);
                    session.setAttribute("directBuyQuantity", qty);
                    session.setAttribute("authMessage", "Please sign in to proceed with your purchase.");

                    if (query == null || !query.contains("buyNowProductId")) {
                        targetUrl = httpRequest.getContextPath() + "/checkout?buyNowProductId=" + pid + "&quantity=" + qty;
                    }
                } catch (NumberFormatException ignored) {}
            }

            logger.info("Unauthenticated request to [{}]. Redirecting to /auth/login", targetUrl);
            session.setAttribute("redirectAfterLogin", targetUrl);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed.");
    }
}