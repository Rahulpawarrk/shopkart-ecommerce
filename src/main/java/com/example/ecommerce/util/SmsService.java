package com.example.ecommerce.util;

import com.example.ecommerce.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Real SMS Gateway Service powered by TextBee Android Gateway (100% Free via Android SIM).
 *
 * <p>Configured via Environment Variables / System Properties:</p>
 * <ul>
 *   <li>{@code TEXTBEE_API_KEY} — API Key from TextBee Dashboard (https://textbee.dev)</li>
 *   <li>{@code TEXTBEE_DEVICE_ID} — Connected Android Device ID from TextBee</li>
 * </ul>
 */
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final String textbeeApiKey;
    private final String textbeeDeviceId;
    private final String androidGatewayUrl;
    private final String twilioSid;
    private final String twilioToken;
    private final String twilioFrom;
    private final HttpClient httpClient;

    public SmsService() {
        this.textbeeApiKey     = getEnv("TEXTBEE_API_KEY", null);
        this.textbeeDeviceId   = getEnv("TEXTBEE_DEVICE_ID", null);
        this.androidGatewayUrl = getEnv("ANDROID_SMS_GATEWAY_URL", null);
        this.twilioSid         = getEnv("TWILIO_ACCOUNT_SID", null);
        this.twilioToken       = getEnv("TWILIO_AUTH_TOKEN", null);
        this.twilioFrom        = getEnv("TWILIO_FROM_PHONE", null);
        this.httpClient        = HttpClient.newBuilder()
                                    .connectTimeout(Duration.ofSeconds(10))
                                    .build();
    }

    /**
     * Sends a 6-digit OTP SMS to the given recipient mobile number using TextBee / real gateway.
     *
     * @param phoneNumber 10-digit mobile number (e.g. 7021317291)
     * @param otpCode     6-digit numeric OTP (e.g. 592814)
     * @return true if dispatched successfully
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || otpCode == null) {
            logger.warn("SMS dispatch aborted: missing phone number or OTP code.");
            throw new ValidationException("Please provide a valid mobile number.");
        }

        String cleanPhone = phoneNumber.trim().replaceAll("[^0-9]", "");
        String message = "Your ShopKart password reset verification code is: " + otpCode + 
                         ". Valid for 10 minutes. Do not share this OTP with anyone.";

        // 1. TextBee Free Android Gateway (Primary)
        if (textbeeApiKey != null && !textbeeApiKey.trim().isEmpty() &&
            textbeeDeviceId != null && !textbeeDeviceId.trim().isEmpty()) {
            
            logger.info("Dispatching SMS via TextBee to +91-{}...", cleanPhone);
            boolean sent = sendViaTextBee(cleanPhone, message);
            if (sent) {
                return true;
            }
            logger.error("TextBee failed to deliver SMS. Check if phone is online in TextBee app.");
            throw new ValidationException("Failed to send SMS via mobile gateway. Please ensure your TextBee device is connected.");
        }

        // 2. Local Android SMS Gateway (Secondary)
        if (androidGatewayUrl != null && !androidGatewayUrl.trim().isEmpty()) {
            boolean sent = sendViaAndroidGateway(cleanPhone, message);
            if (sent) {
                return true;
            }
            throw new ValidationException("Failed to send SMS via local Android SMS gateway.");
        }

        // 3. Twilio Gateway (Optional Cloud Fallback)
        if (twilioSid != null && twilioToken != null && twilioFrom != null) {
            boolean sent = sendViaTwilio(cleanPhone, message);
            if (sent) {
                return true;
            }
            throw new ValidationException("Failed to send SMS via Twilio.");
        }

        // If no real gateway is configured
        logger.error("No SMS Gateway configured! Please set TEXTBEE_API_KEY and TEXTBEE_DEVICE_ID.");
        throw new ValidationException("SMS service is not configured on the server. Please use Email OTP or configure TextBee.");
    }

    private boolean sendViaTextBee(String phone, String message) {
        try {
            String toPhone = phone.startsWith("+") ? phone : "+91" + phone;
            String url = "https://api.textbee.dev/api/v1/gateway/devices/" + textbeeDeviceId.trim() + "/sendSMS";
            
            String jsonPayload = String.format(
                "{\"recipients\":[\"%s\"],\"message\":\"%s\"}",
                toPhone,
                message.replace("\"", "\\\"").replace("\n", "\\n")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("x-api-key", textbeeApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("TextBee API response [{}] body: {}", response.statusCode(), response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("✓ SMS OTP successfully dispatched via TextBee to {}", toPhone);
                return true;
            } else {
                logger.warn("TextBee returned status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception sending SMS via TextBee to {}", phone, e);
            return false;
        }
    }

    private boolean sendViaAndroidGateway(String phone, String message) {
        try {
            String toPhone = phone.startsWith("+") ? phone : "+91" + phone;
            String jsonPayload = String.format(
                "{\"phone\":\"%s\",\"message\":\"%s\"}",
                toPhone,
                message.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(androidGatewayUrl.trim()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() >= 200 && response.statusCode() < 300);
        } catch (Exception e) {
            logger.error("Exception sending SMS via Android Gateway to {}", phone, e);
            return false;
        }
    }

    private boolean sendViaTwilio(String phone, String message) {
        try {
            String toPhone = phone.startsWith("+") ? phone : "+91" + phone;
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioSid + "/Messages.json";
            String formData = "To=" + java.net.URLEncoder.encode(toPhone, StandardCharsets.UTF_8) +
                              "&From=" + java.net.URLEncoder.encode(twilioFrom, StandardCharsets.UTF_8) +
                              "&Body=" + java.net.URLEncoder.encode(message, StandardCharsets.UTF_8);

            String auth = java.util.Base64.getEncoder().encodeToString((twilioSid + ":" + twilioToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 200 || response.statusCode() == 201);
        } catch (Exception e) {
            logger.error("Exception sending SMS via Twilio to {}", phone, e);
            return false;
        }
    }

    private String getEnv(String name, String fallback) {
        String val = System.getenv(name);
        if (val == null || val.trim().isEmpty()) {
            val = System.getProperty(name);
        }
        if (val == null || val.trim().isEmpty()) {
            val = System.getProperty(name.toLowerCase().replace('_', '.'));
        }
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }
}
