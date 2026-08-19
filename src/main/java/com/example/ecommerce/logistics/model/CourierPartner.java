package com.example.ecommerce.logistics.model;

/**
 * Enumeration of supported logistics carriers in India.
 */
public enum CourierPartner {
    BLUEDART("BlueDart Express", "BLUEDART", "BD-", "https://www.bluedart.com/tracking?awb=", "#0284c7", "🚚"),
    DELHIVERY("Delhivery Logistics", "DELHIVERY", "DL-", "https://www.delhivery.com/track/package/", "#dc2626", "📦"),
    DTDC("DTDC Express", "DTDC", "DTDC-", "https://www.dtdc.in/tracking/shipment-tracking.asp?awb=", "#d97706", "✈️"),
    SHIPROCKET("Shiprocket", "SHIPROCKET", "SR-", "https://shiprocket.co/tracking/", "#7c3aed", "🚀"),
    ECOM_EXPRESS("Ecom Express", "ECOM_EXPRESS", "EE-", "https://ecomexpress.in/tracking/?awb=", "#059669", "🛵"),
    SHADOWFAX("Shadowfax", "SHADOWFAX", "SFX-", "https://tracker.shadowfax.in/#/", "#0891b2", "⚡"),
    FEDEX("FedEx India", "FEDEX", "FX-", "https://www.fedex.com/fedextrack/?trknbr=", "#4f46e5", "✈️"),
    SPEED_POST("India Post Speed Post", "SPEED_POST", "IP-", "https://www.indiapost.gov.in/_layouts/15/dop.portal.tracking/trackconsignment.aspx?consNo=", "#b91c1c", "📮"),
    OTHER("Express Logistics Carrier", "OTHER", "AWB", "https://www.google.com/search?q=track+courier+", "#475569", "🚚");

    private final String displayName;
    private final String code;
    private final String awbPrefix;
    private final String trackingBaseUrl;
    private final String brandColor;
    private final String icon;

    CourierPartner(String displayName, String code, String awbPrefix, String trackingBaseUrl, String brandColor, String icon) {
        this.displayName = displayName;
        this.code = code;
        this.awbPrefix = awbPrefix;
        this.trackingBaseUrl = trackingBaseUrl;
        this.brandColor = brandColor;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public String getAwbPrefix() {
        return awbPrefix;
    }

    public String getTrackingBaseUrl() {
        return trackingBaseUrl;
    }

    public String getBrandColor() {
        return brandColor;
    }

    public String getIcon() {
        return icon;
    }

    public String buildTrackingUrl(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            return "#";
        }
        return trackingBaseUrl + trackingNumber.trim();
    }

    /**
     * Resolves carrier partner enum from string name or code.
     */
    public static CourierPartner fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return OTHER;
        }
        String clean = name.trim().toUpperCase();
        if (clean.contains("BLUE") || clean.contains("DART")) return BLUEDART;
        if (clean.contains("DELHIVERY")) return DELHIVERY;
        if (clean.contains("DTDC")) return DTDC;
        if (clean.contains("SHIPROCKET") || clean.contains("ROCKET") || clean.contains("TRACKING") || clean.contains("DELIVERY") || clean.contains("AGGREGATOR") || clean.contains("CARRIER")) return SHIPROCKET;
        if (clean.contains("ECOM")) return ECOM_EXPRESS;
        if (clean.contains("SHADOW")) return SHADOWFAX;
        if (clean.contains("FEDEX")) return FEDEX;
        if (clean.contains("POST") || clean.contains("SPEED")) return SPEED_POST;

        for (CourierPartner cp : values()) {
            if (cp.name().equalsIgnoreCase(clean) || cp.code.equalsIgnoreCase(clean)) {
                return cp;
            }
        }
        return OTHER;
    }

    /**
     * Auto-detects carrier partner from tracking number / AWB pattern.
     */
    public static CourierPartner detectByAwb(String awb) {
        if (awb == null || awb.trim().isEmpty()) {
            return OTHER;
        }
        String clean = awb.trim().toUpperCase();
        if (clean.startsWith("BD") || clean.startsWith("BLUEDART")) return BLUEDART;
        if (clean.startsWith("DL") || clean.startsWith("DELHIVERY")) return DELHIVERY;
        if (clean.startsWith("DTDC") || (clean.startsWith("D") && clean.length() >= 8 && Character.isDigit(clean.charAt(1)))) return DTDC;
        if (clean.startsWith("SR") || clean.startsWith("SHIPROCKET")) return SHIPROCKET;
        if (clean.startsWith("EE") || clean.startsWith("ECOM")) return ECOM_EXPRESS;
        if (clean.startsWith("SFX") || clean.startsWith("SHADOW")) return SHADOWFAX;
        if (clean.startsWith("FX") || clean.startsWith("FEDEX")) return FEDEX;
        if (clean.startsWith("IP") || clean.startsWith("EM") || clean.startsWith("EE")) return SPEED_POST;
        return OTHER;
    }
}
