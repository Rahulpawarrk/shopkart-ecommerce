package com.example.ecommerce.order.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.logistics.model.TrackingEvent;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.logistics.service.LogisticsService;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller delivering Live Telemetry & Carrier Checkpoint Scans for customer and admin tracking views.
 * Routes: /order/live-tracking, /api/order/tracking
 */
@WebServlet(name = "OrderLiveTrackingServlet", urlPatterns = {
        "/order/live-tracking",
        "/api/order/tracking"
})
public class OrderLiveTrackingServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OrderLiveTrackingServlet.class);

    private OrderDAO orderDAO;
    private LogisticsService logisticsService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.orderDAO = new OrderDAO();
        this.logisticsService = new LogisticsService(this.orderDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;

        String idParam = request.getParameter("orderId");
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("id");
        }

        String awbParam = request.getParameter("awb");

        Optional<Order> orderOpt = Optional.empty();

        if (idParam != null && !idParam.trim().isEmpty()) {
            String cleanId = idParam.trim();
            if (cleanId.matches("\\d+")) {
                try {
                    int orderId = Integer.parseInt(cleanId);
                    orderOpt = orderDAO.findById(orderId);
                } catch (NumberFormatException ignored) {}
            } else {
                orderOpt = orderDAO.findByOrderNumber(cleanId);
            }
        } else if (awbParam != null && !awbParam.trim().isEmpty()) {
            orderOpt = orderDAO.findByTrackingNumber(awbParam.trim());
        }

        if (orderOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"success\":false,\"message\":\"Order not found\"}");
            return;
        }

        Order order = orderOpt.get();

        // Access Control: Customer can only view own order, admin can view all
        if (user == null || (!user.isAdmin() && user.getUserId() != order.getUserId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized access to order telemetry\"}");
            return;
        }

        try {
            TrackingResult result = logisticsService.trackLiveShipment(order);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("orderId", order.getOrderId());
            json.put("orderNumber", order.getOrderNumber());
            json.put("courierPartner", result.getCourierPartner() != null ? result.getCourierPartner().name() : "OTHER");
            json.put("courierName", result.getCourierName());
            json.put("courierIcon", result.getCourierPartner() != null ? result.getCourierPartner().getIcon() : "🚚");
            json.put("brandColor", result.getCourierPartner() != null ? result.getCourierPartner().getBrandColor() : "#0284c7");
            json.put("trackingNumber", result.getTrackingNumber());
            json.put("status", result.getNormalizedStatus() != null ? result.getNormalizedStatus().name() : order.getOrderStatus().name());
            json.put("statusDisplayName", result.getNormalizedStatus() != null ? result.getNormalizedStatus().getDisplayName() : order.getOrderStatus().getDisplayName());
            json.put("milestoneStep", result.getNormalizedStatus() != null ? result.getNormalizedStatus().getMilestoneStep() : order.getMilestoneStep());
            json.put("currentLocation", result.getCurrentLocation());
            json.put("destination", result.getDestination());
            json.put("deliveryAgentName", result.getDeliveryAgentName());
            json.put("deliveryAgentPhone", result.getDeliveryAgentPhone());
            json.put("estimatedDelivery", result.getFormattedEstimatedDeliveryDate());
            json.put("remarks", result.getRemarks());
            json.put("carrierTrackingUrl", result.getCarrierTrackingUrl());

            JSONArray scanArray = new JSONArray();
            for (TrackingEvent event : result.getScanHistory()) {
                JSONObject s = new JSONObject();
                s.put("location", event.getLocation());
                s.put("activity", event.getActivity());
                s.put("status", event.getStatus() != null ? event.getStatus().name() : "");
                s.put("rawStatus", event.getRawStatus());
                s.put("time", event.getFormattedTimestamp());
                scanArray.put(s);
            }
            json.put("scans", scanArray);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json.toString());

        } catch (Exception e) {
            logger.error("Error fetching live tracking telemetry for order: {}", order.getOrderId(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Failed to fetch carrier telemetry: " + e.getMessage() + "\"}");
        }
    }
}
