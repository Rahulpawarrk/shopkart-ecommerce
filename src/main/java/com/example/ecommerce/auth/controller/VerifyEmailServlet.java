package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.model.PendingRegistration;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.EmailService;
import com.example.ecommerce.util.SmsService;
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
 * Controller handling Dual-Channel (Email OTP + Mobile SMS OTP) verification
 * on a single page prior to final customer account creation.
 *
 * GET  /verify-email -> Renders Dual OTP verification screen or handles resend.
 * POST /verify-email -> Validates both Email and Mobile OTPs, registers active User, starts session, redirects to home.
 */
@WebServlet(name = "VerifyEmailServlet", urlPatterns = { "/verify-email" })
public class VerifyEmailServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VerifyEmailServlet.class);
    private AuthService authService;
    private EmailService emailService;
    private SmsService smsService;
    private SecureRandom secureRandom;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
        this.emailService = new EmailService();
        this.smsService = new SmsService();
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

        String resend = request.getParameter("resend");

        // Handle Resend Email OTP
        if ("email".equalsIgnoreCase(resend)) {
            try {
                com.example.ecommerce.auth.service.OtpRateLimiter.checkAndIncrement(pending.getEmail(), "REGISTRATION");
                String newEmailOtp = String.format("%06d", secureRandom.nextInt(1000000));
                pending.setEmailOtp(newEmailOtp);
                pending.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
                session.setAttribute("pendingRegistration", pending);

                emailService.sendSignupVerificationOtp(pending.getEmail(), pending.getFirstName(), newEmailOtp);
                request.setAttribute("successMessage", "A new 6-digit verification code has been sent to your Email: " + pending.getEmail());
                logger.info("Resent signup Email OTP to: {}", pending.getEmail());
            } catch (ValidationException ve) {
                request.setAttribute("error", ve.getMessage());
            }
        }
        // Handle Resend Mobile SMS OTP
        else if ("mobile".equalsIgnoreCase(resend) || "sms".equalsIgnoreCase(resend)) {
            try {
                com.example.ecommerce.auth.service.OtpRateLimiter.checkAndIncrement(pending.getPhone(), "REGISTRATION");
                String newMobileOtp = String.format("%06d", secureRandom.nextInt(1000000));
                pending.setMobileOtp(newMobileOtp);
                pending.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
                session.setAttribute("pendingRegistration", pending);

                smsService.sendOtpSms(pending.getPhone(), newMobileOtp);
                request.setAttribute("successMessage", "A new 6-digit SMS verification code has been dispatched to: +91-" + pending.getPhone());
                logger.info("Resent signup Mobile SMS OTP to: {}", pending.getPhone());
            } catch (ValidationException ve) {
                request.setAttribute("error", ve.getMessage());
            } catch (Exception e) {
                logger.warn("Failed to resend SMS: {}", e.getMessage());
                request.setAttribute("error", "Failed to dispatch SMS code. Please verify your phone or try again.");
            }
        }
        // Handle Resend All
        else if ("all".equalsIgnoreCase(resend) || "true".equalsIgnoreCase(resend)) {
            try {
                com.example.ecommerce.auth.service.OtpRateLimiter.checkAndIncrement(pending.getEmail(), "REGISTRATION");
                com.example.ecommerce.auth.service.OtpRateLimiter.checkAndIncrement(pending.getPhone(), "REGISTRATION");

                String newEmailOtp = String.format("%06d", secureRandom.nextInt(1000000));
                String newMobileOtp = String.format("%06d", secureRandom.nextInt(1000000));
                pending.setEmailOtp(newEmailOtp);
                pending.setMobileOtp(newMobileOtp);
                pending.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
                session.setAttribute("pendingRegistration", pending);

                emailService.sendSignupVerificationOtp(pending.getEmail(), pending.getFirstName(), newEmailOtp);
                try {
                    smsService.sendOtpSms(pending.getPhone(), newMobileOtp);
                } catch (Exception ignored) {}

                request.setAttribute("successMessage", "New verification codes sent to both your Email and Mobile.");
                logger.info("Resent both Email & Mobile OTPs to: {} / {}", pending.getEmail(), pending.getPhone());
            } catch (ValidationException ve) {
                request.setAttribute("error", ve.getMessage());
            }
        }

        request.setAttribute("pendingEmail", pending.getEmail());
        request.setAttribute("pendingPhone", pending.getPhone());
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

        String emailOtp = request.getParameter("emailOtp");
        if (emailOtp == null || emailOtp.trim().isEmpty()) {
            emailOtp = request.getParameter("otp"); // backward compatibility
        }
        String mobileOtp = request.getParameter("mobileOtp");

        if (emailOtp != null) emailOtp = emailOtp.trim().replaceAll("\\s+", "");
        if (mobileOtp != null) mobileOtp = mobileOtp.trim().replaceAll("\\s+", "");

        request.setAttribute("pendingEmail", pending.getEmail());
        request.setAttribute("pendingPhone", pending.getPhone());
        request.setAttribute("pendingFirstName", pending.getFirstName());
        request.setAttribute("emailOtp", emailOtp);
        request.setAttribute("mobileOtp", mobileOtp);

        if (emailOtp == null || emailOtp.isEmpty() || mobileOtp == null || mobileOtp.isEmpty()) {
            request.setAttribute("error", "Please enter both the Email Verification OTP and the Mobile SMS OTP.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        if (pending.isOtpExpired()) {
            request.setAttribute("error", "Your verification codes have expired. Please click 'Resend Code' to receive fresh codes.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        boolean emailValid = pending.isEmailOtpValid(emailOtp);
        boolean mobileValid = pending.isMobileOtpValid(mobileOtp);

        if (!emailValid && !mobileValid) {
            logger.warn("Both Email and Mobile OTPs invalid for: {} / {}", pending.getEmail(), pending.getPhone());
            request.setAttribute("error", "Both verification codes are incorrect. Please check your inbox and SMS messages.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        } else if (!emailValid) {
            logger.warn("Invalid Email OTP entered for: {}", pending.getEmail());
            request.setAttribute("error", "Invalid Email verification code. Please check your email inbox.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        } else if (!mobileValid) {
            logger.warn("Invalid Mobile SMS OTP entered for: {}", pending.getPhone());
            request.setAttribute("error", "Invalid Mobile SMS verification code. Please check your SMS messages.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
            return;
        }

        // Both OTPs are valid! Complete final customer registration in Database
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
            com.example.ecommerce.auth.service.OtpRateLimiter.reset(pending.getPhone(), "REGISTRATION");

            logger.info("Dual OTP verification successful! Customer registered and authenticated: {} (+91-{})", 
                    pending.getEmail(), pending.getPhone());
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