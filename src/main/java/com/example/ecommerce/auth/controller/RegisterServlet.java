package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.model.UserSession;
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
 * Controller handling customer registration.
 * GET /register  -> Renders registration form.
 * POST /register -> Processes customer registration, initiates session, redirects to home/profile.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
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

            // Generate secure 6-digit verification OTP
            String otpCode = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
            java.time.LocalDateTime otpExpiry = java.time.LocalDateTime.now().plusMinutes(10);

            com.example.ecommerce.auth.model.PendingRegistration pendingRegistration = 
                    new com.example.ecommerce.auth.model.PendingRegistration(
                            email.trim().toLowerCase(), 
                            password, 
                            firstName.trim(), 
                            lastName.trim(), 
                            phone.trim(), 
                            otpCode, 
                            otpExpiry
                    );

            // Send verification code via Email
            com.example.ecommerce.util.EmailService emailService = new com.example.ecommerce.util.EmailService();
            emailService.sendSignupVerificationOtp(email.trim().toLowerCase(), firstName.trim(), otpCode);

            // Store pending registration in session
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingRegistration", pendingRegistration);

            logger.info("Generated signup email verification OTP for: {}", email.trim().toLowerCase());
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
