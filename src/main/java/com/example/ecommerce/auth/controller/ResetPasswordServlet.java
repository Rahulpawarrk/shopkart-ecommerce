package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.exception.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String phone = request.getParameter("phone");

        if (phone != null && !phone.trim().isEmpty()) {
            request.setAttribute("isOtpMode", true);
            request.setAttribute("phone", phone.trim());
        } else if (token != null && !token.trim().isEmpty()) {
            request.setAttribute("isOtpMode", false);
            request.setAttribute("token", token.trim());
        } else {
            request.setAttribute("invalidToken", true);
            request.setAttribute("tokenError", "No reset token or mobile number provided. Please request a new reset code.");
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token           = request.getParameter("token");
        String phone           = request.getParameter("phone");
        String otpCode         = request.getParameter("otpCode");
        String newPassword     = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        boolean isOtpMode      = (otpCode != null && !otpCode.trim().isEmpty()) || (phone != null && !phone.trim().isEmpty());

        try {
            if (isOtpMode) {
                authService.resetPasswordWithOtp(phone, otpCode, newPassword, confirmPassword);
            } else {
                authService.resetPassword(token, newPassword, confirmPassword);
            }
            // Success: redirect to login with success message
            response.sendRedirect(request.getContextPath() + "/login?reset=success");

        } catch (ValidationException ve) {
            request.setAttribute("isOtpMode", isOtpMode);
            request.setAttribute("token", token);
            request.setAttribute("phone", phone);
            request.setAttribute("otpCode", otpCode);
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")
                   .forward(request, response);

        } catch (Exception e) {
            logger.error("Unexpected error during password reset", e);
            request.setAttribute("isOtpMode", isOtpMode);
            request.setAttribute("token", token);
            request.setAttribute("phone", phone);
            request.setAttribute("otpCode", otpCode);
            request.setAttribute("error", "An unexpected error occurred. Please try again or request a new reset code.");
            request.getRequestDispatcher("/WEB-INF/views/auth/reset-password.jsp")
                   .forward(request, response);
        }
    }
}
