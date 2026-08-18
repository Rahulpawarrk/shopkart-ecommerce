package com.example.ecommerce.logistics.provider;

import com.example.ecommerce.logistics.model.CourierPartner;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.order.model.Order;

/**
 * Common contract for all logistics carriers and delivery partner adapters.
 */
public interface LogisticsProvider {
    
    /**
     * Gets the carrier partner handled by this provider.
     */
    CourierPartner getPartner();

    /**
     * Queries carrier tracking API or simulator for real-time status and scan history.
     */
    TrackingResult track(String trackingNumber, Order order);

    /**
     * Parses real-time carrier webhook payload into unified TrackingResult.
     */
    TrackingResult parseWebhook(String payload, String signature);
}
