package com.example.ecommerce.order.model;

/**
 * Order fulfillment statuses tracking full order lifecycle:
 * Placed -> Processing -> Dispatched -> In Transit -> Out for Delivery -> Delivered.
 */
public enum OrderStatus {
    PENDING("Payment Pending", "badge-warning", 1),
    CONFIRMED("Order Confirmed", "badge-info", 1),
    PROCESSING("Dispatch in Progress", "badge-info", 2),
    DISPATCHED("Dispatched", "badge-primary", 3),
    SHIPPED("In Transit", "badge-primary", 3),
    IN_TRANSIT("In Transit", "badge-primary", 3),
    OUT_FOR_DELIVERY("Out for Delivery", "badge-warning", 4),
    DELIVERED("Delivered", "badge-success", 5),
    CANCELLED("Cancelled", "badge-danger", 0),
    RETURN_REQUESTED("Return Requested", "badge-warning", 0),
    RETURNED("Returned", "badge-secondary", 0);

    private final String displayName;
    private final String badgeClass;
    private final int milestoneStep;

    OrderStatus(String displayName, String badgeClass, int milestoneStep) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.milestoneStep = milestoneStep;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public int getMilestoneStep() {
        return milestoneStep;
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == RETURNED;
    }

    /**
     * Enforces strict sequential order fulfillment lifecycle:
     * CONFIRMED -> PROCESSING -> DISPATCHED -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) return false;
        if (this == target) return true; // Allowed to update logistics/remarks on existing stage

        switch (this) {
            case PENDING:
            case CONFIRMED:
                return target == PROCESSING || target == CANCELLED;
            case PROCESSING:
                return target == DISPATCHED || target == CANCELLED;
            case DISPATCHED:
                return target == IN_TRANSIT || target == SHIPPED || target == CANCELLED;
            case IN_TRANSIT:
            case SHIPPED:
                return target == OUT_FOR_DELIVERY || target == CANCELLED;
            case OUT_FOR_DELIVERY:
                return target == DELIVERED || target == CANCELLED;
            case DELIVERED:
                return target == RETURN_REQUESTED || target == RETURNED;
            case RETURN_REQUESTED:
                return target == RETURNED;
            case CANCELLED:
            case RETURNED:
            default:
                return false;
        }
    }
}

