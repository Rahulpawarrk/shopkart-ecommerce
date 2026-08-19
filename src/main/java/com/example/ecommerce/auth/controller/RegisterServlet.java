package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.model.PendingRegistration;
import com.example.ecommerce.auth.service.AuthService;
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
 * Controller handling customer registration with secure Email OTP verification.
 * GET /register -> Renders registration form.
 * POST /register -> Validates details, generates & sends Email OTP, routes to /verify-email.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = { "/register" })
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
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

        // If already logged in, redirect to home
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");

        try {
            // Validate all registration details and uniqueness in DB before sending OTP
            authService.validateRegistrationDetails(email, password, confirmPassword, firstName, lastName, phone);

            // Generate secure 6-digit Email Verification OTP
            String emailOtp = String.format("%06d", secureRandom.nextInt(1000000));
            LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(10);

            PendingRegistration pendingRegistration = new PendingRegistration(
                    email.trim().toLowerCase(),
                    password,
                    firstName.trim(),
                    lastName.trim(),
                    phone.trim(),
                    emailOtp,
                    otpExpiry);

            // Send verification code via Email
            emailService.sendSignupVerificationOtp(email.trim().toLowerCase(), firstName.trim(), emailOtp);

            // Store pending registration in session
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingRegistration", pendingRegistration);

            logger.info("Generated signup Email OTP for: {}", email.trim().toLowerCase());
            response.sendRedirect(request.getContextPath() + "/verify-email");

        } catch (ValidationException ve) {
            logger.warn("Validation error during registration for {}: {}", email, ve.getMessage());
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("errors", ve.getErrorMessages());
            request.setAttribute("email", email);
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("phone", phone);
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);

        } catch (Exception e) {
            logger.error("Unexpected error during customer registration", e);
            request.setAttribute("error", "An unexpected system error occurred during registration. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
        }
    }
}
