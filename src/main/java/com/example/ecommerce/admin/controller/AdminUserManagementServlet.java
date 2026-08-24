package com.example.ecommerce.admin.controller;

import com.example.ecommerce.audit.model.AuditLog;
import com.example.ecommerce.audit.service.AuditLogService;
import com.example.ecommerce.auth.model.User;
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
import java.util.List;

/**
 * Administrative controller for registering and managing Administrator accounts.
 * Protected by AuthFilter and RoleFilter (requires ADMIN role).
 * Newly registered admins receive strict administrative privileges and are restricted from storefront shopping.
 */
@WebServlet(name = "AdminUserManagementServlet", urlPatterns = {"/admin/admins"})
public class AdminUserManagementServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserManagementServlet.class);

    private AuthService authService;
    private AuditLogService auditLogService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
        this.auditLogService = new AuditLogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<User> admins = authService.getAllAdmins();
        request.setAttribute("adminList", admins);
        request.setAttribute("adminCount", admins.size());

        request.getRequestDispatcher("/WEB-INF/views/admin/admins.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UserSession currentAdmin = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (currentAdmin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login?error=session_expired");
            return;
        }
        int creatorId = currentAdmin.getUserId();

        String firstName       = request.getParameter("firstName");
        String lastName        = request.getParameter("lastName");
        String email           = request.getParameter("email");
        String phone           = request.getParameter("phone");
        String password        = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            User newAdmin = authService.registerAdmin(
                    email,
                    password,
                    confirmPassword,
                    firstName,
                    lastName,
                    phone,
                    creatorId
            );

            // Record Security Audit Log
            try {
                auditLogService.logAction(
                        creatorId,
                        "CREATE_ADMIN",
                        "User",
                        newAdmin.getUserId(),
                        null,
                        "Registered new admin: " + newAdmin.getEmail() + " (" + newAdmin.getFirstName() + " " + newAdmin.getLastName() + ")",
                        request.getRemoteAddr()
                );
            } catch (Exception auditEx) {
                logger.warn("Non-fatal: Failed to write audit log for admin creation: {}", auditEx.getMessage());
            }

            request.getSession().setAttribute("adminSuccessMessage", 
                    "Successfully registered new Administrator: " + newAdmin.getEmail() + ". Account is active with full administrative console access.");
            response.sendRedirect(request.getContextPath() + "/admin/admins");

        } catch (ValidationException ve) {
            logger.warn("Validation error registering admin: {}", ve.getMessage());
            request.setAttribute("error", ve.getMessage());
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);

            List<User> admins = authService.getAllAdmins();
            request.setAttribute("adminList", admins);
            request.setAttribute("adminCount", admins.size());
            request.getRequestDispatcher("/WEB-INF/views/admin/admins.jsp").forward(request, response);

        } catch (Exception e) {
            logger.error("Unexpected error registering admin user: {}", email, e);
            request.setAttribute("error", "An unexpected error occurred while creating the administrator account.");
            request.setAttribute("firstName", firstName);
            request.setAttribute("lastName", lastName);
            request.setAttribute("email", email);
            request.setAttribute("phone", phone);

            List<User> admins = authService.getAllAdmins();
            request.setAttribute("adminList", admins);
            request.setAttribute("adminCount", admins.size());
            request.getRequestDispatcher("/WEB-INF/views/admin/admins.jsp").forward(request, response);
        }
    }
}
