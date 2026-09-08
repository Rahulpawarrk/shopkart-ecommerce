package com.example.ecommerce.auth.controller;

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

/**
 * Controller for managing customer and administrator profile viewing and editing.
 * Protected by AuthFilter.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile", "/admin/profile"})
public class ProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        UserSession userSession = (UserSession) session.getAttribute("currentUser");
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            User user = authService.getUserProfile(userSession.getUserId());
            request.setAttribute("user", user);

            if ("true".equals(request.getParameter("registered"))) {
                request.setAttribute("successMessage", "Welcome to your new account! Registration completed successfully.");
            } else if ("true".equals(request.getParameter("updated"))) {
                request.setAttribute("successMessage", "Your profile details have been updated successfully.");
            }

            if (userSession.isAdmin()) {
                request.getRequestDispatcher("/index.html").forward(request, response);
            } else {
                request.getRequestDispatcher("/index.html").forward(request, response);
            }

        } catch (Exception e) {
            logger.error("Error loading profile for userId: {}", (userSession != null ? userSession.getUserId() : "unknown"), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not load user profile");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        UserSession userSession = (UserSession) session.getAttribute("currentUser");
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");

        try {
            authService.updateProfile(userSession.getUserId(), firstName, lastName, phone);

            // Update session cache
            User updatedUser = authService.getUserProfile(userSession.getUserId());
            session.setAttribute("currentUser", UserSession.fromUser(updatedUser));

            String redirectTarget = userSession.isAdmin() 
                    ? request.getContextPath() + "/admin/profile?updated=true" 
                    : request.getContextPath() + "/profile?updated=true";
            response.sendRedirect(redirectTarget);

        } catch (ValidationException ve) {
            request.setAttribute("error", ve.getMessage());
            User user = authService.getUserProfile(userSession.getUserId());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            request.setAttribute("user", user);
            
            String view = userSession.isAdmin() ? "/index.html" : "/index.html";
            request.getRequestDispatcher(view).forward(request, response);
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            request.setAttribute("error", "Failed to update profile. Please try again.");
            String view = userSession.isAdmin() ? "/index.html" : "/index.html";
            request.getRequestDispatcher(view).forward(request, response);
        }
    }
}

