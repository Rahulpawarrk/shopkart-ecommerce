package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dao.RoleDAO;
import com.example.ecommerce.auth.dao.UserDAO;
import com.example.ecommerce.auth.dao.PasswordResetDAO;
import com.example.ecommerce.auth.model.Role;
import com.example.ecommerce.auth.model.User;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.AppException;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.EmailService;
import com.example.ecommerce.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.ecommerce.auth.model.ForgotPasswordResult;

import com.example.ecommerce.util.SmsService;

/**
 * Service orchestrating authentication, customer registration, session preparation, and profile management.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordResetDAO passwordResetDAO;
    private final EmailService emailService;
    private final SmsService smsService;

    public AuthService() {
        this.roleDAO = new RoleDAO();
        this.userDAO = new UserDAO(this.roleDAO);
        this.passwordResetDAO = new PasswordResetDAO();
        this.emailService = new EmailService();
        this.smsService = new SmsService();
    }

    public AuthService(UserDAO userDAO, RoleDAO roleDAO) {
        this(userDAO, roleDAO, new PasswordResetDAO(), new EmailService(), new SmsService());
    }

    public AuthService(UserDAO userDAO, RoleDAO roleDAO, PasswordResetDAO passwordResetDAO) {
        this(userDAO, roleDAO, passwordResetDAO, new EmailService(), new SmsService());
    }

    public AuthService(UserDAO userDAO, RoleDAO roleDAO, PasswordResetDAO passwordResetDAO, EmailService emailService, SmsService smsService) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.passwordResetDAO = passwordResetDAO;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    /**
     * Validates customer registration details prior to sending email verification OTP.
     * Checks format of email, 10-digit mobile, password length/match, and database uniqueness.
     */
    public void validateRegistrationDetails(String email, String password, String confirmPassword, 
                                           String firstName, String lastName, String phone) {
        List<String> errors = new ArrayList<>();

        // 1. Validation Rules
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("Please enter a valid email address.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            errors.add("Mobile number is required.");
        } else {
            String cleanPhone = phone.trim().replaceAll("[^0-9]", "");
            if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
                errors.add("Please enter a valid 10-digit mobile number.");
            }
        }
        if (password == null || password.length() < 8) {
            errors.add("Password must be at least 8 characters long.");
        }
        if (password != null && !password.equals(confirmPassword)) {
            errors.add("Passwords do not match.");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First name is required.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.add("Last name is required.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim().replaceAll("[^0-9]", "");

        // 2. Uniqueness Checks for BOTH Email and Phone
        if (userDAO.existsByEmail(normalizedEmail)) {
            logger.warn("Registration validation failed: Email {} already exists.", normalizedEmail);
            throw new ValidationException("An account with this email address already exists.");
        }
        if (userDAO.existsByPhone(normalizedPhone)) {
            logger.warn("Registration validation failed: Mobile number {} already exists.", normalizedPhone);
            throw new ValidationException("An account with this mobile number already exists.");
        }
    }

    /**
     * Registers a new customer with transactional safety.
     * Automatically assigns the 'CUSTOMER' role and provisions an empty Cart and Wishlist.
     * Both Email and Mobile Phone are compulsory and must be unique in the system.
     *
     * @return UserSession representing the newly registered customer
     */
    public UserSession registerCustomer(String email, String password, String confirmPassword, 
                                        String firstName, String lastName, String phone) {
        
        validateRegistrationDetails(email, password, confirmPassword, firstName, lastName, phone);

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim().replaceAll("[^0-9]", "");

        // 3. Hash Password with BCrypt
        String passwordHash = PasswordUtil.hashPassword(password);

        User newUser = new User();
        newUser.setEmail(normalizedEmail);
        newUser.setPasswordHash(passwordHash);
        newUser.setFirstName(firstName.trim());
        newUser.setLastName(lastName.trim());
        newUser.setPhone(normalizedPhone);
        newUser.setStatus("ACTIVE");

        // 4. Resolve CUSTOMER Role
        Role customerRole = roleDAO.findByName("CUSTOMER")
                .orElseThrow(() -> new AppException("System role 'CUSTOMER' not configured in database."));

        // 5. Transactional Execution: Create User -> Assign Role -> Provision Cart & Wishlist
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                int userId = userDAO.createUser(newUser, conn);
                roleDAO.assignRoleToUser(userId, customerRole.getRoleId(), conn);
                userDAO.provisionCartAndWishlist(userId, conn);

                conn.commit(); // Commit Transaction
                logger.info("Successfully registered customer: [id={}, email={}, phone={}]", userId, normalizedEmail, normalizedPhone);

                newUser.setUserId(userId);
                newUser.setRoles(List.of(customerRole));
                return UserSession.fromUser(newUser);

            } catch (SQLException e) {
                conn.rollback(); // Rollback on any failure
                logger.error("Transaction rolled back during customer registration for: {}", normalizedEmail, e);
                throw new DatabaseException("Failed to complete registration transaction", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during registration", e);
            throw new DatabaseException("Database connectivity error", e);
        }
    }

    /**
     * Authenticates a user by email and plain-text password against stored BCrypt hash.
     *
     * @return UserSession for active authenticated user
     */
    public UserSession login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ValidationException("Email and password are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userDAO.findByEmail(normalizedEmail);

        if (userOpt.isEmpty()) {
            logger.warn("Authentication failed: User not found with email {}", normalizedEmail);
            throw new ValidationException("Invalid email or password.");
        }

        User user = userOpt.get();

        // Check account status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            logger.warn("Authentication rejected: Account {} is {}", normalizedEmail, user.getStatus());
            throw new ValidationException("Your account is " + user.getStatus().toLowerCase() + ". Please contact customer support.");
        }

        // Verify BCrypt Password
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: Incorrect password for {}", normalizedEmail);
            throw new ValidationException("Invalid email or password.");
        }

        logger.info("User {} successfully authenticated.", normalizedEmail);
        return UserSession.fromUser(user);
    }

    /**
     * Retrieves full user profile by user ID.
     */
    public User getUserProfile(int userId) {
        return userDAO.findById(userId)
                .orElseThrow(() -> new AppException("User profile not found", 404));
    }

    /**
     * Updates profile details.
     */
    public void updateProfile(int userId, String firstName, String lastName, String phone) {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("First name and last name cannot be empty.");
        }
        userDAO.updateProfile(userId, firstName, lastName, phone);
        logger.info("Updated profile for userId {}", userId);
    }

    /**
     * Changes user password after validating current password.
     */
    public void changePassword(int userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("New password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new ValidationException("New passwords do not match.");
        }

        User user = getUserProfile(userId);
        if (!PasswordUtil.checkPassword(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        userDAO.updatePassword(userId, newHash);
        logger.info("Password successfully changed for userId {}", userId);
    }

    /**
     * Initiates the forgot-password flow supporting both EMAIL and MOBILE PHONE SMS OTP.
     *
     * @param identifier   email address OR 10-digit mobile number
     * @param appBaseUrl   full base URL used to build the reset link
     * @return ForgotPasswordResult with method, masked info, and verification details
     */
    public ForgotPasswordResult initiateForgotPassword(String identifier, String appBaseUrl) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return new ForgotPasswordResult("EMAIL", "", "", null, null, false);
        }

        String input = identifier.trim();
        boolean isEmail = input.contains("@");
        String method = isEmail ? "EMAIL" : "PHONE";

        Optional<User> userOpt;
        if (isEmail) {
            userOpt = userDAO.findByEmail(input.toLowerCase());
        } else {
            String cleanPhone = input.replaceAll("[^0-9]", "");
            userOpt = userDAO.findByPhone(cleanPhone);
        }

        if (userOpt.isEmpty()) {
            logger.info("Forgot password requested for non-existent {}: {} (ignored)", method, input);
            return new ForgotPasswordResult(method, isEmail ? maskEmail(input) : maskPhone(input), input, null, null, false);
        }

        User user = userOpt.get();
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            logger.warn("Forgot password requested for non-ACTIVE account: {}", input);
            return new ForgotPasswordResult(method, isEmail ? maskEmail(input) : maskPhone(input), input, null, null, false);
        }

        if (isEmail) {
            // Email Token Generation (48-byte token, 1-hour expiry)
            byte[] bytes = new byte[48];
            SECURE_RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            passwordResetDAO.createToken(user.getUserId(), token);

            String resetLink = appBaseUrl + "/reset-password?token=" + token;

            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
            }

            logger.info("Forgot-password token issued via EMAIL for userId: {}", user.getUserId());
            return new ForgotPasswordResult("EMAIL", maskEmail(user.getEmail()), user.getEmail(), resetLink, null, true);

        } else {
            // Mobile SMS 6-digit numeric OTP Generation (10-minute expiry)
            int randomOtp = 100000 + SECURE_RANDOM.nextInt(900000);
            String otpCode = String.valueOf(randomOtp);

            passwordResetDAO.createOtp(user.getUserId(), otpCode);

            String userPhone = user.getPhone() != null ? user.getPhone() : input;
            smsService.sendOtpSms(userPhone, otpCode);

            logger.info("Forgot-password OTP [{}] issued via SMS to +91-{} for userId: {}", otpCode, userPhone, user.getUserId());
            return new ForgotPasswordResult("PHONE", maskPhone(userPhone), userPhone, null, otpCode, true);
        }
    }

    /**
     * Backward-compatible overload for email-only requests.
     */
    public void inititateForgotPassword(String email, String appBaseUrl) {
        initiateForgotPassword(email, appBaseUrl);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIdx = email.indexOf('@');
        String name = email.substring(0, atIdx);
        String domain = email.substring(atIdx);
        if (name.length() <= 2) return name + "***" + domain;
        return name.substring(0, 2) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }

    /**
     * Validates a reset token and updates the user's password, then invalidates the token.
     *
     * @param token              the reset token from the email link
     * @param newPassword        new plain-text password
     * @param confirmPassword    confirmation field
     */
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new ValidationException("Invalid or missing reset token.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        Integer userId = passwordResetDAO.findValidToken(token.trim())
                .orElseThrow(() -> new ValidationException(
                        "This reset link is invalid or has expired. Please request a new one."));

        String newHash = PasswordUtil.hashPassword(newPassword);
        userDAO.updatePassword(userId, newHash);
        passwordResetDAO.invalidateToken(token.trim());

        logger.info("Password successfully reset via token for userId: {}", userId);
    }

    /**
     * Validates a 6-digit SMS OTP and updates the user's password.
     *
     * @param phone              10-digit mobile number
     * @param otpCode            6-digit OTP code received via SMS
     * @param newPassword        new plain-text password
     * @param confirmPassword    confirmation password
     */
    public void resetPasswordWithOtp(String phone, String otpCode, String newPassword, String confirmPassword) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Mobile number is required.");
        }
        if (otpCode == null || otpCode.trim().length() < 6) {
            throw new ValidationException("Please enter the 6-digit OTP verification code.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        String cleanPhone = phone.trim().replaceAll("[^0-9]", "");
        User user = userDAO.findByPhone(cleanPhone)
                .orElseThrow(() -> new ValidationException("No account found with this mobile number."));

        boolean isValid = passwordResetDAO.validateOtp(user.getUserId(), otpCode.trim());
        if (!isValid) {
            throw new ValidationException("Invalid or expired OTP verification code. Please request a new OTP.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        userDAO.updatePassword(user.getUserId(), newHash);
        passwordResetDAO.invalidateToken(otpCode.trim());

        logger.info("Password successfully reset via SMS OTP for userId: {}", user.getUserId());
    }
}
