package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.model.PendingRegistration;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Controller handling 6-digit Email Verification OTP validation prior to final account creation.
 * GET  /verify-email -> Renders OTP verification screen or handles resend request.
 * POST /verify-email -> Validates OTP, creates active User account, starts login session, redirects to home.
 */
@WebServlet(name = "VerifyEmailServlet", urlPatterns = { "/verify-email" })
public class VerifyEmailServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VerifyEmailServlet.class);
    private AuthService authService;
    private EmailService emailService;
    private SecureRandom secureRandom;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
        this.emailService = new EmailService();
        this.secureRandom = new SecureRandom();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        PendingRegistration pending = (session != null)
                ? (PendingRegistration) session.getAttribute("pendingRegistration")
                : null;

        if (pending == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        // Handle Resend Request
        if ("true".equalsIgnoreCase(request.getParameter("resend")) || "email".equalsIgnoreCase(request.getParameter("resend"))) {
            try {
                com.example.ecommerce.auth.service.OtpRateLimiter.checkAndIncrement(pending.getEmail(), "REGISTRATION");

                String newOtp = String.format("%06d", secureRandom.nextInt(1000000));
                pending.setEmailOtp(newOtp);
                pending.setOtpHash(org.mindrot.jbcrypt.BCrypt.hashpw(newOtp, org.mindrot.jbcrypt.BCrypt.gensalt()));
                pending.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
                
                emailService.sendSignupVerificationOtp(pending.getEmail(), pending.getFirstName(), newOtp);
                pending.clearOtp();
                session.setAttribute("pendingRegistration", pending);

                request.setAttribute("successMessage",
                        "A fresh 6-digit verification code has been sent to " + pending.getEmail());
                logger.info("Resent signup email verification OTP to: {}", pending.getEmail());
            } catch (ValidationException ve) {
                request.setAttribute("error", ve.getMessage());
            }
        }

        request.setAttribute("pendingEmail", pending.getEmail());
        request.setAttribute("pendingFirstName", pending.getFirstName());
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        PendingRegistration pending = (session != null)
                ? (PendingRegistration) session.getAttribute("pendingRegistration")
                : null;

        if (pending == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        String inputOtp = request.getParameter("otp");
        if (inputOtp == null || inputOtp.trim().isEmpty()) {
            inputOtp = request.getParameter("emailOtp");
        }
        if (inputOtp != null) {
            inputOtp = inputOtp.trim().replaceAll("\\s+", "");
        }

        request.setAttribute("pendingEmail", pending.getEmail());
        request.setAttribute("pendingFirstName", pending.getFirstName());

        if (inputOtp == null || inputOtp.isEmpty()) {
            request.setAttribute("error", "Please enter the 6-digit verification code sent to your email.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        if (pending.isOtpExpired()) {
            request.setAttribute("error", "Your verification code has expired. Please click 'Resend Code' to receive a new one.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        if (!pending.isEmailOtpValid(inputOtp)) {
            logger.warn("Invalid OTP entered for pending registration: {}", pending.getEmail());
            request.setAttribute("error", "Invalid verification code. Please check your email inbox and try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        // OTP is verified! Complete final registration in Database
        try {
            UserSession userSession = authService.registerCustomer(
                    pending.getEmail(),
                    pending.getPassword(),
                    pending.getPassword(),
                    pending.getFirstName(),
                    pending.getLastName(),
                    pending.getPhone());

            // Clean up pending registration state & establish authenticated session
            session.removeAttribute("pendingRegistration");
            session.setAttribute("currentUser", userSession);
            session.setAttribute("cart", new Cart());
            com.example.ecommerce.auth.service.OtpRateLimiter.reset(pending.getEmail(), "REGISTRATION");

            logger.info("Email verified successfully! Registered and authenticated customer: {}", pending.getEmail());
            response.sendRedirect(request.getContextPath() + "/?registered=true&welcome=true");

        } catch (ValidationException ve) {
            logger.warn("Validation failure completing registration for {}: {}", pending.getEmail(), ve.getMessage());
            request.setAttribute("error", ve.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);

        } catch (Exception e) {
            // Graceful Fallback: If user was already created during a rapid double-submission
            try {
                UserSession existingSession = authService.login(pending.getEmail(), pending.getPassword());
                session.removeAttribute("pendingRegistration");
                session.setAttribute("currentUser", existingSession);
                session.setAttribute("cart", new Cart());
                logger.info("Customer account already created, authenticated directly: {}", pending.getEmail());
                response.sendRedirect(request.getContextPath() + "/?registered=true&welcome=true");
                return;
            } catch (Exception ignored) {
            }

            logger.error("Unexpected error finalizing customer registration for: {}", pending.getEmail(), e);
            request.setAttribute("error", "An unexpected system error occurred while creating your account. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
        }
    }
}