package com.example.ecommerce.util;

import com.example.ecommerce.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Real Cloud SMS Gateway Service supporting Twilio Virtual Cloud Gateway and TextBee.
 *
 * <p>Twilio Environment Variables:</p>
 * <ul>
 *   <li>{@code TWILIO_ACCOUNT_SID} — Twilio Account SID (starts with AC...)</li>
 *   <li>{@code TWILIO_AUTH_TOKEN} or {@code TWILIO_API_KEY_SECRET} — Twilio Auth Token or API Secret</li>
 *   <li>{@code TWILIO_API_KEY_SID} — Optional API Key SID (starts with SK...)</li>
 *   <li>{@code TWILIO_FROM_PHONE} — Dedicated Twilio Cloud Phone Number (e.g. +1234567890)</li>
 * </ul>
 */
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final String twilioAccountSid;
    private final String twilioAuthUser;
    private final String twilioAuthSecret;
    private final String twilioFromPhone;
    private final String textbeeApiKey;
    private final String textbeeDeviceId;
    private final String androidGatewayUrl;
    private final HttpClient httpClient;

    public SmsService() {
        this.twilioAccountSid = getEnv("TWILIO_ACCOUNT_SID", getEnv("TWILIO_SID", null));
        String apiKeySid      = getEnv("TWILIO_API_KEY_SID", getEnv("TWILIO_KEY_SID", null));
        this.twilioAuthUser   = (apiKeySid != null && !apiKeySid.trim().isEmpty()) ? apiKeySid.trim() : this.twilioAccountSid;
        this.twilioAuthSecret = getEnv("TWILIO_AUTH_TOKEN", getEnv("TWILIO_API_KEY_SECRET", getEnv("TWILIO_SECRET", null)));
        this.twilioFromPhone  = getEnv("TWILIO_FROM_PHONE", getEnv("TWILIO_PHONE_NUMBER", getEnv("TWILIO_FROM", null)));

        this.textbeeApiKey     = getEnv("TEXTBEE_API_KEY", null);
        this.textbeeDeviceId   = getEnv("TEXTBEE_DEVICE_ID", null);
        this.androidGatewayUrl = getEnv("ANDROID_SMS_GATEWAY_URL", null);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
    }

    /**
     * Sends a 6-digit OTP SMS with default/password-reset purpose.
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        return sendOtpSms(phoneNumber, otpCode, "PASSWORD_RESET");
    }

    /**
     * Sends a 6-digit OTP SMS with a purpose-tailored concise message.
     *
     * @param phoneNumber 10-digit mobile number (e.g. 9503705064)
     * @param otpCode     6-digit numeric OTP (e.g. 592814)
     * @param purpose     "REGISTRATION" or "PASSWORD_RESET"
     * @return true if dispatched successfully
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode, String purpose) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || otpCode == null) {
            logger.warn("SMS dispatch aborted: missing phone number or OTP code.");
            throw new ValidationException("Please provide a valid mobile number.");
        }

        String cleanDigits = phoneNumber.trim().replaceAll("[^0-9]", "");
        String cleanPhone = cleanDigits.length() == 12 && cleanDigits.startsWith("91") ? cleanDigits.substring(2) : cleanDigits;

        String message;
        if ("REGISTRATION".equalsIgnoreCase(purpose)) {
            message = "ShopKart OTP: " + otpCode + ". Valid for 10 mins. Welcome!";
        } else {
            message = "ShopKart OTP: " + otpCode + ". Valid for 10 mins.";
        }

        // 1. Twilio Cloud Gateway (Primary - 100% Private Cloud)
        if (twilioAccountSid != null && !twilioAccountSid.trim().isEmpty() &&
            twilioAuthSecret != null && !twilioAuthSecret.trim().isEmpty() &&
            twilioFromPhone != null && !twilioFromPhone.trim().isEmpty()) {
            
            logger.info("Dispatching [{}] SMS via Twilio Cloud to +91-{}...", purpose, cleanPhone);
            boolean sent = sendViaTwilio(cleanPhone, message);
            if (sent) {
                return true;
            }
            logger.error("Twilio dispatch failed. Please check Twilio account balance or credentials.");
            throw new ValidationException("Failed to deliver SMS via Twilio. Please check your Twilio configuration or trial verified numbers.");
        }

        // 2. TextBee Android Gateway (Secondary)
        if (textbeeApiKey != null && !textbeeApiKey.trim().isEmpty() &&
            textbeeDeviceId != null && !textbeeDeviceId.trim().isEmpty()) {
            
            logger.info("Dispatching [{}] SMS via TextBee to {}...", purpose, cleanPhone);
            boolean sent = sendViaTextBee(cleanPhone, message);
            if (sent) {
                return true;
            }
            logger.error("TextBee failed to deliver SMS. Check if phone is online in TextBee app.");
            throw new ValidationException("Failed to send SMS via mobile gateway. Please ensure your TextBee device is connected.");
        }

        // 3. Local Android SMS Gateway
        if (androidGatewayUrl != null && !androidGatewayUrl.trim().isEmpty()) {
            boolean sent = sendViaAndroidGateway(cleanPhone, message);
            if (sent) {
                return true;
            }
            throw new ValidationException("Failed to send SMS via local Android SMS gateway.");
        }

        // If no real gateway is configured
        logger.error("No SMS Gateway configured! Please set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_FROM_PHONE.");
        throw new ValidationException("SMS service is not configured on the server. Please use Email OTP or configure Twilio.");
    }

    private boolean sendViaTwilio(String phone, String message) {
        try {
            // E.164 destination format for Twilio (e.g. +919503705064)
            String toPhone = phone.startsWith("+") ? phone : (phone.length() == 10 ? "+91" + phone : "+" + phone);
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid.trim() + "/Messages.json";

            String formData = "To=" + URLEncoder.encode(toPhone, StandardCharsets.UTF_8) +
                              "&From=" + URLEncoder.encode(twilioFromPhone.trim(), StandardCharsets.UTF_8) +
                              "&Body=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            String credentials = twilioAuthUser.trim() + ":" + twilioAuthSecret.trim();
            String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Twilio API response [{}] body: {}", response.statusCode(), response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("✓ SMS OTP successfully sent via Twilio Cloud to {}", toPhone);
                return true;
            } else {
                logger.warn("Twilio returned error status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception sending SMS via Twilio to {}", phone, e);
            return false;
        }
    }

    private boolean sendViaTextBee(String phone, String message) {
        try {
            String cleanDigits = phone.trim().replaceAll("[^0-9]", "");
            String toPhone = (cleanDigits.length() == 12 && cleanDigits.startsWith("91")) 
                    ? cleanDigits.substring(2) 
                    : cleanDigits;

            String url = "https://api.textbee.dev/api/v1/gateway/send-sms";
            
            String jsonPayload = String.format(
                "{\"recipients\":[\"%s\"],\"message\":\"%s\",\"deviceId\":\"%s\"}",
                toPhone,
                message.replace("\"", "\\\"").replace("\n", "\\n"),
                textbeeDeviceId.trim()
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
