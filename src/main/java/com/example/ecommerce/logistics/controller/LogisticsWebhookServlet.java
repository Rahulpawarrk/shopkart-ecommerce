package com.example.ecommerce.logistics.controller;

import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.logistics.service.LogisticsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Webhook Ingress Endpoint for Live Carrier Push Notifications.
 * Handled URLs:
 *  - /api/logistics/webhook/bluedart
 *  - /api/logistics/webhook/delhivery
 *  - /api/logistics/webhook/dtdc
 *  - /api/logistics/webhook/shiprocket
 *  - /api/logistics/webhook/generic
 */
@WebServlet(name = "LogisticsWebhookServlet", urlPatterns = {"/api/logistics/webhook/*"})
public class LogisticsWebhookServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsWebhookServlet.class);

    private LogisticsService logisticsService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.logisticsService = new LogisticsService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String webhookSecret = System.getenv("LOGISTICS_WEBHOOK_SECRET");
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            webhookSecret = System.getProperty("LOGISTICS_WEBHOOK_SECRET");
        }
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            webhookSecret = System.getProperty("logistics.webhook.secret");
        }
        String signature = request.getHeader("X-Carrier-Signature");
        String webhookToken = request.getHeader("X-Webhook-Token");
        if (webhookToken == null) {
            webhookToken = request.getParameter("token");
        }

        // Webhook Authentication Check
        if (webhookSecret != null && !webhookSecret.trim().isEmpty()) {
            boolean authorized = (signature != null && signature.trim().equals(webhookSecret.trim()))
                    || (webhookToken != null && webhookToken.trim().equals(webhookSecret.trim()));
            if (!authorized) {
                logger.warn("Unauthorized carrier webhook attempt from IP: {}", request.getRemoteAddr());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"status\":\"ERROR\",\"message\":\"Unauthorized: Missing or invalid carrier signature/token.\"}");
                return;
            }
        }

        String pathInfo = request.getPathInfo();
        String carrierCode = (pathInfo != null && pathInfo.length() > 1) ? pathInfo.substring(1) : "generic";

        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        }

        if (payload.length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"ERROR\",\"message\":\"Empty webhook payload.\"}");
            return;
        }

        logger.info("Received authenticated carrier webhook for [{}]: payload length = {} bytes", carrierCode, payload.length());

        try {
            TrackingResult result = logisticsService.processCarrierWebhook(carrierCode, payload.toString(), signature);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.format("{\"status\":\"SUCCESS\",\"carrier\":\"%s\",\"awb\":\"%s\"}", 
                    carrierCode, result != null ? result.getTrackingNumber() : ""));
        } catch (Exception e) {
            logger.error("Error processing carrier webhook for: {}", carrierCode, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
