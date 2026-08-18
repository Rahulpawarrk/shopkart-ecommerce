package com.example.ecommerce.logistics.model;

import com.example.ecommerce.order.model.OrderStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an individual logistics checkpoint scan or event.
 */
public class TrackingEvent {
    private String location;
    private String activity;
    private OrderStatus status;
    private String rawStatus;
    private LocalDateTime timestamp;

    public TrackingEvent() {
    }

    public TrackingEvent(String location, String activity, OrderStatus status, String rawStatus, LocalDateTime timestamp) {
        this.location = location;
        this.activity = activity;
        this.status = status;
        this.rawStatus = rawStatus;
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRawStatus() {
        return rawStatus;
    }

    public void setRawStatus(String rawStatus) {
        this.rawStatus = rawStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return timestamp.format(formatter);
    }
}
