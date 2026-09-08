package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.*;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.auth.service.LoginRateLimiter;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for user authentication, registration, session management, and password recovery.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    private final AuthService authService;
    private final CartService cartService;

    @Autowired
    public AuthRestController(AuthService authService, CartService cartService) {
        this.authService = authService;
        this.cartService = cartService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String remoteIp = httpRequest.getRemoteAddr();
        String email = request.getEmail().trim().toLowerCase();

        if (LoginRateLimiter.isBlocked(remoteIp, email)) {
            long remaining = LoginRateLimiter.getRemainingLockoutMinutes(remoteIp, email);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many failed attempts. Try again in " + remaining + " minute(s).", "RATE_LIMITED"));
        }

        try {
            UserSession userSession = authService.login(email, request.getPassword());

            // Renew session to prevent session fixation
            HttpSession oldSession = httpRequest.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute("currentUser", userSession);

            // Preload cart for customer
            if (!userSession.isAdmin()) {
                try {
                    newSession.setAttribute("cart", cartService.getCart(userSession.getUserId()));
                } catch (Exception ignored) {}
            }

            LoginRateLimiter.clearFailures(remoteIp, email);
            logger.info("User {} logged in successfully via REST API", userSession.getEmail());

            return ResponseEntity.ok(ApiResponse.ok("Login successful", UserDto.fromSession(userSession)));

        } catch (ValidationException ve) {
            LoginRateLimiter.recordFailure(remoteIp, email);
            logger.warn("Authentication failed for {}: {}", email, ve.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ve.getMessage(), "INVALID_CREDENTIALS"));
        } catch (Exception e) {
            LoginRateLimiter.recordFailure(remoteIp, email);
            logger.error("Error during REST login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal login error. Please try again.", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest httpRequest) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match", "PASSWORD_MISMATCH"));
        }

        UserSession userSession = authService.registerCustomer(
                req.getEmail().trim().toLowerCase(),
                req.getPassword(),
                req.getConfirmPassword(),
                req.getFirstName().trim(),
                req.getLastName().trim(),
                req.getPhone() != null ? req.getPhone().trim() : ""
        );

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("currentUser", userSession);

        logger.info("New user registered successfully: {}", userSession.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Account created successfully", UserDto.fromSession(userSession)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated", "UNAUTHORIZED"));
        }

        return ResponseEntity.ok(ApiResponse.ok(UserDto.fromSession(userSession)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
            session.removeAttribute("cart");
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please login to change your password", "UNAUTHORIZED"));
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New passwords do not match", "PASSWORD_MISMATCH"));
        }

        authService.changePassword(userSession.getUserId(), req.getCurrentPassword(), req.getNewPassword(), req.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.initiateForgotPassword(req.getEmail().trim().toLowerCase(), null);
        return ResponseEntity.ok(ApiResponse.ok("If the email exists, a password reset link has been dispatched.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Passwords do not match", "PASSWORD_MISMATCH"));
        }
        authService.resetPassword(req.getToken(), req.getPassword(), req.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. You may now login.", null));
    }
}
