package com.example.ecommerce.logistics.provider;

import com.example.ecommerce.logistics.model.CourierPartner;
import com.example.ecommerce.logistics.model.TrackingEvent;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Logistics Provider supporting Ecom Express, Shadowfax, FedEx, Speed Post and custom carriers.
 */
public class GenericLogisticsProvider implements LogisticsProvider {
    private final CourierPartner partner;

    public GenericLogisticsProvider(CourierPartner partner) {
        this.partner = partner != null ? partner : CourierPartner.OTHER;
    }

    @Override
    public CourierPartner getPartner() {
        return partner;
    }

    @Override
    public TrackingResult track(String trackingNumber, Order order) {
        TrackingResult result = new TrackingResult();
        result.setSuccess(true);
        result.setCourierPartner(partner);
        result.setCourierName(partner.getDisplayName());
        result.setTrackingNumber(trackingNumber);
        result.setCarrierTrackingUrl(partner.buildTrackingUrl(trackingNumber));

        String destCity = (order != null && order.getShippingCity() != null) ? order.getShippingCity() : "Customer Destination";
        String destState = (order != null && order.getShippingState() != null) ? order.getShippingState() : "";
        result.setDestination(destCity + (destState.isEmpty() ? "" : ", " + destState));
        result.setOrigin("Central Logistics Hub");

        OrderStatus currentOrderStat = (order != null && order.getOrderStatus() != null) ? order.getOrderStatus() : OrderStatus.IN_TRANSIT;
        result.setNormalizedStatus(currentOrderStat);
        result.setDeliveryAgentName("Logistics Courier Agent");
        result.setDeliveryAgentPhone(order != null && order.getDeliveryAgentPhone() != null ? order.getDeliveryAgentPhone() : "+91-9876543210");
        result.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(2));

        List<TrackingEvent> events = new ArrayList<>();
        LocalDateTime baseTime = (order != null && order.getCreatedAt() != null) ? order.getCreatedAt() : LocalDateTime.now().minusDays(1);

        events.add(new TrackingEvent("Fulfillment Warehouse", "Package Picked & Electronic Shipping Manifest Created", OrderStatus.PROCESSING, "MANIFESTED", baseTime.plusHours(1)));
        events.add(new TrackingEvent(partner.getDisplayName() + " Origin Facility", "Consignment Inwarded into Express Network", OrderStatus.DISPATCHED, "INSCAN", baseTime.plusHours(3)));

        if (currentOrderStat == OrderStatus.IN_TRANSIT || currentOrderStat == OrderStatus.OUT_FOR_DELIVERY || currentOrderStat == OrderStatus.DELIVERED) {
            events.add(new TrackingEvent("Regional Inter-Hub Sort Facility", "In Transit: Package Dispatched to Destination Hub", OrderStatus.IN_TRANSIT, "IN_TRANSIT", baseTime.plusHours(12)));
            events.add(new TrackingEvent(destCity + " Distribution Center", "Package Arrived at Local Destination Delivery Facility", OrderStatus.IN_TRANSIT, "ARRIVED_LOCAL", baseTime.plusHours(20)));
            result.setCurrentLocation(destCity + " Distribution Center");
            result.setRemarks("In transit to local delivery hub");
        }

        if (currentOrderStat == OrderStatus.OUT_FOR_DELIVERY || currentOrderStat == OrderStatus.DELIVERED) {
            events.add(new TrackingEvent(destCity + " Delivery Hub", "Out for Delivery: Dispatched with Delivery Associate", OrderStatus.OUT_FOR_DELIVERY, "OUT_FOR_DELIVERY", baseTime.plusHours(24)));
            result.setCurrentLocation(destCity + " Delivery Hub");
            result.setRemarks("Out for doorstep delivery");
        }

        if (currentOrderStat == OrderStatus.DELIVERED) {
            events.add(new TrackingEvent(result.getDestination(), "Delivered: Successfully handed over to consignee", OrderStatus.DELIVERED, "DELIVERED", baseTime.plusHours(27)));
            result.setCurrentLocation(result.getDestination());
            result.setRemarks("Delivered Successfully");
            result.setDeliveredAt(baseTime.plusHours(27));
        }

        result.setScanHistory(events);
        return result;
    }

    @Override
    public TrackingResult parseWebhook(String payload, String signature) {
        TrackingResult result = new TrackingResult();
        result.setSuccess(true);
        result.setCourierPartner(partner);
        result.setRemarks("Generic webhook update received");
        return result;
    }
}
