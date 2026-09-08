package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
        String error = request.getParameter("error");
        if (error != null && !error.trim().isEmpty()) {
            request.setAttribute("error", error.trim());
        }
        request.getRequestDispatcher("/index.html")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String identifier = request.getParameter("identifier");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("email");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("phone");
        }
        if (identifier != null) {
            identifier = identifier.trim();
        }

        String otpCode = request.getParameter("otpCode");

        // If action is verify_otp or an otpCode parameter is present in submission
        if ("verify_otp".equalsIgnoreCase(action) || (otpCode != null && !otpCode.trim().isEmpty())) {
            try {
                String resetToken = authService.verifyResetOtp(identifier, otpCode);
                logger.info("Reset OTP verified for identifier: {}. Redirecting to reset password page.", identifier);
                
                // Securely attach token to user session to prevent leakage via browser URL bar and Referer headers
                HttpSession session = request.getSession(true);
                session.setAttribute("passwordResetToken", resetToken);
                session.setAttribute("passwordResetIdentifier", identifier);

                response.sendRedirect(request.getContextPath() + "/reset-password");
                return;
            } catch (com.example.ecommerce.exception.ValidationException ve) {
                request.setAttribute("error", ve.getMessage());
                request.setAttribute("otpSent", true);
                request.setAttribute("identifier", identifier);
                request.setAttribute("rawIdentifier", identifier);
                request.setAttribute("methodType", (identifier != null && identifier.contains("@")) ? "EMAIL" : "PHONE");
                String destination = request.getParameter("destination");
                request.setAttribute("destination", destination != null && !destination.isEmpty() ? destination : identifier);
                request.getRequestDispatcher("/index.html")
                       .forward(request, response);
                return;
            } catch (Exception e) {
                logger.error("Unexpected error during OTP verification for identifier: {}", identifier, e);
                request.setAttribute("error", "An error occurred during verification. Please try again.");
                request.setAttribute("otpSent", true);
                request.setAttribute("identifier", identifier);
                request.setAttribute("rawIdentifier", identifier);
                request.setAttribute("methodType", (identifier != null && identifier.contains("@")) ? "EMAIL" : "PHONE");
                request.getRequestDispatcher("/index.html")
                       .forward(request, response);
                return;
            }
        }

        // Otherwise, action is send_otp (generate OTP)
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
        } catch (com.example.ecommerce.exception.ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("identifier", identifier);
            request.getRequestDispatcher("/index.html")
                   .forward(request, response);
            return;
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

        request.setAttribute("otpSent", true);
        request.setAttribute("submitted", true); // for backward compatibility
        request.setAttribute("identifier", identifier);
        request.setAttribute("methodType", result != null ? result.getMethod() : "EMAIL");
        request.setAttribute("destination", result != null ? result.getMaskedDestination() : identifier);
        request.setAttribute("rawIdentifier", result != null ? result.getRawDestination() : identifier);
        request.setAttribute("userFound", result != null && result.isUserFound());

        request.getRequestDispatcher("/index.html")
               .forward(request, response);
    }
}

