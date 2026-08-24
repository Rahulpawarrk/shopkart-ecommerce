package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.auth.service.LoginRateLimiter;
import com.example.ecommerce.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller handling user authentication and session establishment.
 * GET /login  -> Renders login view.
 * POST /login -> Validates credentials, creates secure session, redirects user.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/auth/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String errorParam = request.getParameter("error");
        if ("auth_required".equalsIgnoreCase(errorParam)) {
            request.setAttribute("warning", "Please sign in to access the requested page.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Rate limit check — block after 5 failed attempts in 15 minutes
        String remoteIp = request.getRemoteAddr();
        if (LoginRateLimiter.isBlocked(remoteIp, email)) {
            long remaining = LoginRateLimiter.getRemainingLockoutMinutes(remoteIp, email);
            request.setAttribute("error", "Too many failed login attempts. Please try again in " + remaining + " minute(s).");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            UserSession userSession = authService.login(email, password);

            // Retrieve possible target URL saved before auth redirect
            HttpSession oldSession = request.getSession(false);
            String redirectUrl = null;
            if (oldSession != null) {
                redirectUrl = (String) oldSession.getAttribute("redirectAfterLogin");
                oldSession.invalidate(); // Invalidate old session to protect against session fixation attacks
            }

            // Create new session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentUser", userSession);

            // Preload user's persistent cart from DB into session
            try {
                com.example.ecommerce.cart.service.CartService cartService = new com.example.ecommerce.cart.service.CartService();
                com.example.ecommerce.cart.model.Cart userCart = cartService.getCart(userSession.getUserId());
                newSession.setAttribute("cart", userCart);
            } catch (Exception e) {
                logger.warn("Could not preload cart for user {}: {}", userSession.getUserId(), e.getMessage());
            }

            logger.info("User {} logged in successfully. Roles: {}", userSession.getEmail(), userSession.getRoles());
            LoginRateLimiter.clearFailures(remoteIp, email);

            // Redirect logic with Open Redirect prevention
            if (userSession.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else if (redirectUrl != null && isSafeRedirect(request, redirectUrl)) {
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }

        } catch (ValidationException ve) {
            LoginRateLimiter.recordFailure(remoteIp, email);
            logger.warn("Authentication failed for {}: {}", email, ve.getMessage());
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);

        } catch (Exception e) {
            LoginRateLimiter.recordFailure(remoteIp, email);
            logger.error("Unexpected error during login", e);
            request.setAttribute("error", "An internal error occurred during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    /**
     * Validates that the redirect URL is strictly an internal path within this web application.
     */
    private boolean isSafeRedirect(HttpServletRequest request, String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        String contextPath = request.getContextPath();
        // Prevent protocol-relative URLs (//evil.com), absolute URLs (https://evil.com), and auth loops
        return (url.startsWith(contextPath + "/") || url.startsWith("/"))
                && !url.startsWith("//")
                && !url.contains("://")
                && !url.contains("/login")
                && !url.contains("/register");
    }
}
