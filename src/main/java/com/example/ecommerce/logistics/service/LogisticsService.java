package com.example.ecommerce.logistics.service;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.logistics.model.CourierPartner;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.logistics.provider.*;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.OrderStatusHistory;
import com.example.ecommerce.order.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Orchestrator service routing multi-carrier live tracking requests and webhook callbacks.
 */
public class LogisticsService {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsService.class);

    private final OrderDAO orderDAO;
    private final Map<CourierPartner, LogisticsProvider> providerRegistry;

    public LogisticsService() {
        this.orderDAO = new OrderDAO();
        this.providerRegistry = new EnumMap<>(CourierPartner.class);
        registerProviders();
    }

    public LogisticsService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
        this.providerRegistry = new EnumMap<>(CourierPartner.class);
        registerProviders();
    }

    private void registerProviders() {
        providerRegistry.put(CourierPartner.BLUEDART, new BlueDartLogisticsProvider());
        providerRegistry.put(CourierPartner.DELHIVERY, new DelhiveryLogisticsProvider());
        providerRegistry.put(CourierPartner.DTDC, new DTDCLogisticsProvider());
        providerRegistry.put(CourierPartner.SHIPROCKET, new ShiprocketLogisticsProvider());
        providerRegistry.put(CourierPartner.ECOM_EXPRESS, new GenericLogisticsProvider(CourierPartner.ECOM_EXPRESS));
        providerRegistry.put(CourierPartner.SHADOWFAX, new GenericLogisticsProvider(CourierPartner.SHADOWFAX));
        providerRegistry.put(CourierPartner.FEDEX, new GenericLogisticsProvider(CourierPartner.FEDEX));
        providerRegistry.put(CourierPartner.SPEED_POST, new GenericLogisticsProvider(CourierPartner.SPEED_POST));
        providerRegistry.put(CourierPartner.OTHER, new GenericLogisticsProvider(CourierPartner.OTHER));
    }

    /**
     * Resolves appropriate logistics provider by courier partner name or tracking number format.
     */
    public LogisticsProvider getProvider(String courierPartnerName, String trackingNumber) {
        CourierPartner partner = CourierPartner.fromName(courierPartnerName);
        if (partner == CourierPartner.OTHER && trackingNumber != null) {
            partner = CourierPartner.detectByAwb(trackingNumber);
        }
        return providerRegistry.getOrDefault(partner, providerRegistry.get(CourierPartner.OTHER));
    }

    /**
     * Queries carrier tracking API for real-time order telemetry and updates internal milestones if advanced.
     */
    public TrackingResult trackLiveShipment(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        String trackingNumber = order.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            trackingNumber = "AWB" + String.format("%08d", order.getOrderId());
        }

        LogisticsProvider provider = getProvider(order.getCourierPartner(), trackingNumber);
        TrackingResult result = provider.track(trackingNumber, order);

        // If carrier reports progress beyond DB order milestone, auto-advance fulfillment state
        if (result != null && result.isSuccess() && result.getNormalizedStatus() != null) {
            advanceMilestoneIfProgressed(order, result);
        }

        return result;
    }

    /**
     * Advances internal order fulfillment milestone only if carrier indicates true forward progress.
     */
    private void advanceMilestoneIfProgressed(Order order, TrackingResult result) {
        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus carrierStatus = result.getNormalizedStatus();

        if (carrierStatus != null && currentStatus != null) {
            // Strict forward progression: only advance if carrier milestone step is strictly higher
            if (carrierStatus.getMilestoneStep() > currentStatus.getMilestoneStep() && !currentStatus.isTerminal()) {
                try (Connection conn = DBConnection.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        String agentPhone = result.getDeliveryAgentPhone() != null ? result.getDeliveryAgentPhone() : order.getDeliveryAgentPhone();
                        String courier = result.getCourierName() != null ? result.getCourierName() : order.getCourierPartner();

                        orderDAO.updateOrderFulfillment(order.getOrderId(), carrierStatus, courier, result.getTrackingNumber(), agentPhone, conn);

                        if (carrierStatus == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.PENDING) {
                            orderDAO.updatePaymentStatus(order.getOrderId(), PaymentStatus.PAID, conn);
                        }

                        OrderStatusHistory history = new OrderStatusHistory(
                                order.getOrderId(),
                                currentStatus,
                                carrierStatus,
                                0, // Automated Carrier System ID
                                "Live Telemetry Sync [" + (result.getCourierPartner() != null ? result.getCourierPartner().getDisplayName() : "Carrier") + "]: " + 
                                (result.getRemarks() != null ? result.getRemarks() : "Milestone advanced to " + carrierStatus.getDisplayName())
                        );
                        orderDAO.createStatusHistory(history, conn);

                        conn.commit();
                        order.setOrderStatus(carrierStatus);
                        logger.info("Order [{}] fulfillment milestone auto-advanced from [{}] to [{}] via Live Carrier Telemetry", 
                                order.getOrderId(), currentStatus, carrierStatus);
                    } catch (SQLException e) {
                        conn.rollback();
                        logger.warn("Failed to auto-advance order fulfillment milestone for order: {}", order.getOrderId(), e);
                    }
                } catch (SQLException e) {
                    logger.warn("Database error during live telemetry sync for order: {}", order.getOrderId(), e);
                }
            }
        }
    }

    /**
     * Processes live push webhooks from logistics carriers (BlueDart, Delhivery, DTDC, Shiprocket).
     */
    public TrackingResult processCarrierWebhook(String carrierCode, String payload, String signature) {
        CourierPartner partner = CourierPartner.fromName(carrierCode);
        LogisticsProvider provider = providerRegistry.getOrDefault(partner, providerRegistry.get(CourierPartner.OTHER));

        TrackingResult result = provider.parseWebhook(payload, signature);
        if (result != null && result.isSuccess() && result.getTrackingNumber() != null) {
            Optional<Order> orderOpt = orderDAO.findByTrackingNumber(result.getTrackingNumber());
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                advanceMilestoneIfProgressed(order, result);
            } else {
                logger.warn("Received webhook for untracked AWB: {}", result.getTrackingNumber());
            }
        }
        return result;
    }
}
