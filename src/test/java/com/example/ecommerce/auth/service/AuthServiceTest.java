package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.dao.RoleDAO;
import com.example.ecommerce.auth.dao.UserDAO;
import com.example.ecommerce.auth.model.Role;
import com.example.ecommerce.auth.model.User;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests with Mockito")
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private RoleDAO roleDAO;

    @Mock
    private com.example.ecommerce.auth.dao.PasswordResetDAO passwordResetDAO;

    @Mock
    private com.example.ecommerce.util.EmailService emailService;

    @Mock
    private com.example.ecommerce.util.SmsService smsService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should throw ValidationException when registration input is invalid")
    void testRegisterValidationFailures() {
        // Invalid email, short password, mismatched confirmation
        assertThrows(ValidationException.class, () -> 
            authService.registerCustomer("invalid-email", "short", "mismatch", "", "", "")
        );
    }

    @Test
    @DisplayName("Should throw ValidationException when email is already registered")
    void testRegisterDuplicateEmail() {
        when(userDAO.existsByEmail("test@example.com")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            authService.registerCustomer("test@example.com", "Password@123", "Password@123", "John", "Doe", "9876543210")
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should throw ValidationException when mobile number is already registered")
    void testRegisterDuplicatePhone() {
        when(userDAO.existsByEmail("unique@example.com")).thenReturn(false);
        when(userDAO.existsByPhone("9876543210")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            authService.registerCustomer("unique@example.com", "Password@123", "Password@123", "John", "Doe", "9876543210")
        );

        assertTrue(ex.getMessage().contains("mobile number already exists"));
    }

    @Test
    @DisplayName("Should successfully validate unique and well-formatted registration details")
    void testValidateRegistrationDetailsSuccess() {
        when(userDAO.existsByEmail("valid@example.com")).thenReturn(false);
        when(userDAO.existsByPhone("9876543210")).thenReturn(false);

        assertDoesNotThrow(() ->
            authService.validateRegistrationDetails("valid@example.com", "Password@123", "Password@123", "Alice", "Smith", "9876543210")
        );
    }

    @Test
    @DisplayName("Should successfully authenticate active user with matching password")
    void testLoginSuccess() {
        String plainPassword = "SecurePassword@123";
        String passwordHash = PasswordUtil.hashPassword(plainPassword);

        User mockUser = new User();
        mockUser.setUserId(10);
        mockUser.setEmail("user@example.com");
        mockUser.setPasswordHash(passwordHash);
        mockUser.setFirstName("Alice");
        mockUser.setLastName("Smith");
        mockUser.setStatus("ACTIVE");
        mockUser.setRoles(List.of(new Role(2, "CUSTOMER", "Customer Role")));

        when(userDAO.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        UserSession session = authService.login("user@example.com", plainPassword);

        assertNotNull(session);
        assertEquals(10, session.getUserId());
        assertEquals("user@example.com", session.getEmail());
        assertEquals("Alice Smith", session.getFullName());
        assertTrue(session.isCustomer());
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("Should reject login when password does not match")
    void testLoginIncorrectPassword() {
        String passwordHash = PasswordUtil.hashPassword("CorrectPassword123");

        User mockUser = new User();
        mockUser.setUserId(10);
        mockUser.setEmail("user@example.com");
        mockUser.setPasswordHash(passwordHash);
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            authService.login("user@example.com", "WrongPassword123")
        );

        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("Should reject login when user account is SUSPENDED or INACTIVE")
    void testLoginInactiveAccount() {
        User mockUser = new User();
        mockUser.setUserId(12);
        mockUser.setEmail("blocked@example.com");
        mockUser.setPasswordHash(PasswordUtil.hashPassword("Password@123"));
        mockUser.setStatus("SUSPENDED");

        when(userDAO.findByEmail("blocked@example.com")).thenReturn(Optional.of(mockUser));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            authService.login("blocked@example.com", "Password@123")
        );

        assertTrue(ex.getMessage().contains("suspended"));
    }

    @Test
    @DisplayName("Should reject password change when current password is wrong")
    void testChangePasswordWrongCurrent() {
        User mockUser = new User();
        mockUser.setUserId(5);
        mockUser.setPasswordHash(PasswordUtil.hashPassword("OldPassword@123"));

        when(userDAO.findById(5)).thenReturn(Optional.of(mockUser));

        assertThrows(ValidationException.class, () ->
            authService.changePassword(5, "WrongCurrentPassword", "NewPassword@123", "NewPassword@123")
        );
    }

    @Test
    @DisplayName("Should generate 6-digit OTP and send email when email exists")
    void testInitiateForgotPasswordEmailSuccess() {
        User mockUser = new User();
        mockUser.setUserId(20);
        mockUser.setEmail("emailuser@example.com");
        mockUser.setFirstName("Alice");
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByEmail("emailuser@example.com")).thenReturn(Optional.of(mockUser));

        var result = authService.initiateForgotPassword("emailuser@example.com", "http://localhost:8080");

        assertNotNull(result);
        assertEquals("EMAIL", result.getMethod());
        assertTrue(result.isUserFound());
        verify(passwordResetDAO).createOtp(eq(20), anyString());
        verify(emailService).sendPasswordResetOtp(eq("emailuser@example.com"), eq("Alice"), anyString());
    }

    @Test
    @DisplayName("Should generate 6-digit OTP when mobile number exists")
    void testInitiateForgotPasswordMobileSuccess() {
        User mockUser = new User();
        mockUser.setUserId(20);
        mockUser.setPhone("9876543210");
        mockUser.setEmail("mobileuser@example.com");
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByPhone("9876543210")).thenReturn(Optional.of(mockUser));

        var result = authService.initiateForgotPassword("9876543210", "http://localhost:8080");

        assertNotNull(result);
        assertEquals("PHONE", result.getMethod());
        assertTrue(result.isUserFound());
        verify(passwordResetDAO).createOtp(eq(20), anyString());
        verify(smsService).sendOtpSms(eq("9876543210"), anyString());
    }

    @Test
    @DisplayName("Should successfully reset password with valid Email OTP")
    void testResetPasswordWithEmailOtpSuccess() {
        User mockUser = new User();
        mockUser.setUserId(25);
        mockUser.setEmail("user@example.com");
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordResetDAO.validateOtp(25, "123456")).thenReturn(true);

        assertDoesNotThrow(() ->
            authService.resetPasswordWithOtp("user@example.com", "123456", "NewPassword@123", "NewPassword@123")
        );

        verify(userDAO).updatePassword(eq(25), anyString());
        verify(passwordResetDAO).invalidateToken("123456");
    }

    @Test
    @DisplayName("Should successfully reset password with valid SMS OTP")
    void testResetPasswordWithOtpSuccess() {
        User mockUser = new User();
        mockUser.setUserId(25);
        mockUser.setPhone("9876543210");
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByPhone("9876543210")).thenReturn(Optional.of(mockUser));
        when(passwordResetDAO.validateOtp(25, "123456")).thenReturn(true);

        assertDoesNotThrow(() ->
            authService.resetPasswordWithOtp("9876543210", "123456", "NewPassword@123", "NewPassword@123")
        );

        verify(userDAO).updatePassword(eq(25), anyString());
        verify(passwordResetDAO).invalidateToken("123456");
    }

    @Test
    @DisplayName("Should reject password reset when OTP is invalid")
    void testResetPasswordWithOtpInvalid() {
        User mockUser = new User();
        mockUser.setUserId(25);
        mockUser.setPhone("9876543210");
        mockUser.setStatus("ACTIVE");

        when(userDAO.findByPhone("9876543210")).thenReturn(Optional.of(mockUser));
        when(passwordResetDAO.validateOtp(25, "000000")).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () ->
            authService.resetPasswordWithOtp("9876543210", "000000", "NewPassword@123", "NewPassword@123")
        );

        assertTrue(ex.getMessage().contains("Invalid or expired OTP"));
    }

    @Test
    @DisplayName("Should successfully register new Admin through admin dashboard")
    void testRegisterAdminSuccess() {
        when(userDAO.existsByEmail("newadmin@shopkart.com")).thenReturn(false);
        when(userDAO.existsByPhone("9988776655")).thenReturn(false);
        when(userDAO.createAdminUser(any(User.class))).thenReturn(101);

        User created = authService.registerAdmin(
                "newadmin@shopkart.com",
                "AdminSecure@123",
                "AdminSecure@123",
                "Admin",
                "User",
                "9988776655",
                1
        );

        assertNotNull(created);
        assertEquals(101, created.getUserId());
        assertEquals("newadmin@shopkart.com", created.getEmail());
        verify(userDAO).createAdminUser(any(User.class));
    }
}
