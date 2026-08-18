package com.example.ecommerce.logistics.model;

import com.example.ecommerce.order.model.OrderStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unified response DTO for live telemetry returned by logistics carrier APIs.
 */
public class TrackingResult {
    private boolean success;
    private CourierPartner courierPartner;
    private String courierName;
    private String trackingNumber;
    private OrderStatus normalizedStatus;
    private String rawCarrierStatus;
    private String currentLocation;
    private String origin;
    private String destination;
    private String deliveryAgentName;
    private String deliveryAgentPhone;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime deliveredAt;
    private String remarks;
    private String carrierTrackingUrl;
    private List<TrackingEvent> scanHistory = new ArrayList<>();

    public TrackingResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CourierPartner getCourierPartner() {
        return courierPartner;
    }

    public void setCourierPartner(CourierPartner courierPartner) {
        this.courierPartner = courierPartner;
        if (courierPartner != null) {
            this.courierName = courierPartner.getDisplayName();
        }
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public OrderStatus getNormalizedStatus() {
        return normalizedStatus;
    }

    public void setNormalizedStatus(OrderStatus normalizedStatus) {
        this.normalizedStatus = normalizedStatus;
    }

    public String getRawCarrierStatus() {
        return rawCarrierStatus;
    }

    public void setRawCarrierStatus(String rawCarrierStatus) {
        this.rawCarrierStatus = rawCarrierStatus;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDeliveryAgentName() {
        return deliveryAgentName;
    }

    public void setDeliveryAgentName(String deliveryAgentName) {
        this.deliveryAgentName = deliveryAgentName;
    }

    public String getDeliveryAgentPhone() {
        return deliveryAgentPhone;
    }

    public void setDeliveryAgentPhone(String deliveryAgentPhone) {
        this.deliveryAgentPhone = deliveryAgentPhone;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getFormattedEstimatedDeliveryDate() {
        if (estimatedDeliveryDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return estimatedDeliveryDate.format(formatter);
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCarrierTrackingUrl() {
        return carrierTrackingUrl;
    }

    public void setCarrierTrackingUrl(String carrierTrackingUrl) {
        this.carrierTrackingUrl = carrierTrackingUrl;
    }

    public List<TrackingEvent> getScanHistory() {
        return scanHistory != null ? scanHistory : Collections.emptyList();
    }

    public void setScanHistory(List<TrackingEvent> scanHistory) {
        this.scanHistory = scanHistory != null ? scanHistory : new ArrayList<>();
    }

    public void addScanEvent(TrackingEvent event) {
        if (this.scanHistory == null) this.scanHistory = new ArrayList<>();
        this.scanHistory.add(event);
    }
}
