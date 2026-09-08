package com.example.ecommerce.api.dto;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingResponse {
    private String orderNumber;
    private String status;
    private String courierPartner;
    private String trackingNumber;
    private String trackingUrl;
    private String estimatedDelivery;
    private List<TrackingEventDto> events = new ArrayList<>();

    public OrderTrackingResponse() {}

    public static class TrackingEventDto {
        private String timestamp;
        private String status;
        private String location;
        private String description;

        public TrackingEventDto() {}

        public TrackingEventDto(String timestamp, String status, String location, String description) {
            this.timestamp = timestamp;
            this.status = status;
            this.location = location;
            this.description = description;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCourierPartner() {
        return courierPartner;
    }

    public void setCourierPartner(String courierPartner) {
        this.courierPartner = courierPartner;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public List<TrackingEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<TrackingEventDto> events) {
        this.events = events;
    }
}
