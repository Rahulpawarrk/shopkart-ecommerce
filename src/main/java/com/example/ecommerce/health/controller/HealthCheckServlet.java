package com.example.ecommerce.health.controller;

import com.example.ecommerce.health.model.SystemHealth;
import com.example.ecommerce.health.service.HealthCheckService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller Servlet for health checks and system diagnostics.
 * Mapped to /health and /api/health
 */
@WebServlet(name = "HealthCheckServlet", urlPatterns = {"/health", "/api/health"})
public class HealthCheckServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServlet.class);
    private HealthCheckService healthCheckService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.healthCheckService = new HealthCheckService();
        logger.info("HealthCheckServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String servletInfo = getServletContext().getServerInfo() + " (Servlet " + 
                             getServletContext().getMajorVersion() + "." + 
                             getServletContext().getMinorVersion() + ")";
        
        SystemHealth health = healthCheckService.getSystemHealth(servletInfo);

        String path = request.getServletPath();
        String format = request.getParameter("format");
        String acceptHeader = request.getHeader("Accept");

        // If JSON format is requested or path is /api/health
        if ("/api/health".equals(path) || "json".equalsIgnoreCase(format) || 
            (acceptHeader != null && acceptHeader.contains("application/json"))) {
            
            response.setContentType("application/json;charset=UTF-8");
            if (!health.isDatabaseConnected()) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
            } else {
                response.setStatus(HttpServletResponse.SC_OK); // 200
            }

            String jsonResponse = String.format(
                "{\"appName\":\"%s\",\"appVersion\":\"%s\",\"javaVersion\":\"%s\",\"databaseConnected\":%b,\"totalMemoryMb\":%d,\"usedMemoryMb\":%d,\"freeMemoryMb\":%d,\"timestamp\":\"%s\"}",
                escapeJson(health.getAppName()),
                escapeJson(health.getAppVersion()),
                escapeJson(health.getJavaVersion()),
                health.isDatabaseConnected(),
                health.getTotalMemoryMb(),
                health.getUsedMemoryMb(),
                health.getFreeMemoryMb(),
                health.getTimestamp().toString()
            );
            response.getWriter().write(jsonResponse);
            return;
        }

        // Standard MVC: /health is strictly for Administrators
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        com.example.ecommerce.auth.model.UserSession user = (session != null) ? 
                (com.example.ecommerce.auth.model.UserSession) session.getAttribute("currentUser") : null;
        if (user == null || !user.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login?error=admin_required");
            return;
        }

        // Bind model attribute and forward to protected JSP View
        request.setAttribute("health", health);
        request.getRequestDispatcher("/WEB-INF/views/health.jsp").forward(request, response);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
