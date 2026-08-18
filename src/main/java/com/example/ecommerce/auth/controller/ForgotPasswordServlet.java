package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.example.ecommerce.auth.model.ForgotPasswordResult;

/**
 * Controller for the Forgot Password flow (Supports Email & Mobile Number).
 * GET  /forgot-password  → shows reset option form (Email / Mobile)
 * POST /forgot-password  → generates token, sends email/SMS simulation, shows confirmation
 *
 * This route is NOT protected by AuthFilter (allowed through explicitly).
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordServlet.class);

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String identifier = request.getParameter("identifier");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("email");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("phone");
        }

        // Build the app base URL from the incoming request
        String scheme      = request.getScheme();
        String serverName  = request.getServerName();
        int    serverPort  = request.getServerPort();
        String contextPath = request.getContextPath();

        String appBaseUrl;
        if (("http".equals(scheme) && serverPort == 80) ||
            ("https".equals(scheme) && serverPort == 443)) {
            appBaseUrl = scheme + "://" + serverName + contextPath;
        } else {
            appBaseUrl = scheme + "://" + serverName + ":" + serverPort + contextPath;
        }

        ForgotPasswordResult result = null;
        try {
            result = authService.initiateForgotPassword(identifier, appBaseUrl);
        } catch (Exception e) {
            logger.error("Unexpected error during forgot-password for identifier: {}", identifier, e);
            result = new ForgotPasswordResult(
                (identifier != null && identifier.contains("@")) ? "EMAIL" : "PHONE",
                identifier != null ? identifier : "",
                identifier != null ? identifier : "",
                null,
                null,
                false
            );
        }

        request.setAttribute("submitted", true);
        request.setAttribute("identifier", identifier);
        request.setAttribute("methodType", result != null ? result.getMethod() : "EMAIL");
        request.setAttribute("destination", result != null ? result.getMaskedDestination() : identifier);
        request.setAttribute("rawPhone", result != null ? result.getRawDestination() : identifier);
        request.setAttribute("resetLink", result != null ? result.getResetLink() : null);
        request.setAttribute("otpCode", result != null ? result.getOtpCode() : null);
        request.setAttribute("userFound", result != null && result.isUserFound());

        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp")
               .forward(request, response);
    }
}
