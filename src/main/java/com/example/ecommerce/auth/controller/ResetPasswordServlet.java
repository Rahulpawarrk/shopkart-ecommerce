package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.service.AuthService;
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
 * Controller for the Reset Password flow (supports token-based link and 6-digit SMS OTP).
 * GET  /reset-password?token=...  → Email token reset form
 * GET  /reset-password?phone=...  → Mobile SMS OTP reset form
 * POST /reset-password            → verifies token/OTP, updates password, redirects to login
 *
 * This route is NOT protected by AuthFilter.
 */
@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordServlet.class);

    private AuthService authService;

    private com.example.ecommerce.auth.dao.PasswordResetDAO passwordResetDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
        this.passwordResetDAO = new com.example.ecommerce.auth.dao.PasswordResetDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String identifier = request.getParameter("identifier");

        // Fallback to session if set by ForgotPasswordServlet
        HttpSession session = request.getSession(false);
        if ((token == null || token.trim().isEmpty()) && session != null) {
            Object sessToken = session.getAttribute("passwordResetToken");
            if (sessToken != null) {
                token = sessToken.toString();
            }
        }
        if ((identifier == null || identifier.trim().isEmpty()) && session != null) {
            Object sessId = session.getAttribute("passwordResetIdentifier");
            if (sessId != null) {
                identifier = sessId.toString();
            }
        }

        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("email");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("phone");
        }

        // If a token is provided, verify it exists and hasn't expired
        if (token != null && !token.trim().isEmpty()) {
            token = token.trim();
            if (passwordResetDAO.findValidToken(token).isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/forgot-password?error=" +
                        java.net.URLEncoder.encode("Your reset session has expired or is invalid. Please request a new OTP.", java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
        } else if (identifier == null || identifier.trim().isEmpty()) {
            // Neither token nor identifier provided, redirect to forgot-password
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.setAttribute("identifier", identifier != null ? identifier.trim() : "");
        request.setAttribute("token", token != null ? token.trim() : "");

        request.getRequestDispatcher("/index.html")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String identifier      = request.getParameter("identifier");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("email");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = request.getParameter("phone");
        }

        String token           = request.getParameter("token");
        String otpCode         = request.getParameter("otpCode");
        String newPassword     = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        HttpSession session = request.getSession(false);
        if ((token == null || token.trim().isEmpty()) && session != null) {
            Object sessToken = session.getAttribute("passwordResetToken");
            if (sessToken != null) {
                token = sessToken.toString();
            }
        }

        try {
            if (token != null && !token.trim().isEmpty()) {
                authService.resetPassword(token, newPassword, confirmPassword);
            } else if (otpCode != null && !otpCode.trim().isEmpty()) {
                authService.resetPasswordWithOtp(identifier, otpCode, newPassword, confirmPassword);
            } else {
                throw new ValidationException("Invalid or expired session. Please start over from Forgot Password.");
            }
            
            // Clean up session attributes
            if (session != null) {
                session.removeAttribute("passwordResetToken");
                session.removeAttribute("passwordResetIdentifier");
            }

            // Success: redirect to login with success message
            logger.info("Password successfully updated. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/login?reset=success");

        } catch (ValidationException ve) {
            request.setAttribute("identifier", identifier);
            request.setAttribute("token", token);
            request.setAttribute("otpCode", otpCode);
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/index.html")
                   .forward(request, response);

        } catch (Exception e) {
            logger.error("Unexpected error during password reset", e);
            request.setAttribute("identifier", identifier);
            request.setAttribute("token", token);
            request.setAttribute("otpCode", otpCode);
            request.setAttribute("error", "An unexpected error occurred. Please try again or request a new reset code.");
            request.getRequestDispatcher("/index.html")
                   .forward(request, response);
        }
    }
}

