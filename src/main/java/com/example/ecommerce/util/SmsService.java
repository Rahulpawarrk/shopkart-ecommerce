package com.example.ecommerce.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * SMS Gateway Service supporting real SMS dispatch and graceful simulation fallback.
 *
 * <p>Supports environment variables for zero-code-change cloud deployment:</p>
 * <ul>
 *   <li>{@code SMS_API_KEY} or {@code FAST2SMS_API_KEY} — Fast2SMS / Indian SMS Gateway API Key</li>
 *   <li>{@code TWILIO_ACCOUNT_SID}, {@code TWILIO_AUTH_TOKEN}, {@code TWILIO_FROM_PHONE} — Twilio Cloud Gateway</li>
 * </ul>
 *
 * <p>When no SMS API keys are configured, the service logs the OTP and runs in
 * Development/Sandbox mode so flows can be tested instantly without paid SMS services.</p>
 */
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final String fast2smsApiKey;
    private final String twilioSid;
    private final String twilioToken;
    private final String twilioFrom;
    private final HttpClient httpClient;

    public SmsService() {
        this.fast2smsApiKey = getEnv("FAST2SMS_API_KEY", getEnv("SMS_API_KEY", null));
        this.twilioSid      = getEnv("TWILIO_ACCOUNT_SID", null);
        this.twilioToken    = getEnv("TWILIO_AUTH_TOKEN", null);
        this.twilioFrom     = getEnv("TWILIO_FROM_PHONE", null);
        this.httpClient     = HttpClient.newBuilder()
                                    .connectTimeout(Duration.ofSeconds(10))
                                    .build();
    }

    /**
     * Sends a 6-digit OTP SMS to the given recipient mobile number.
     *
     * @param phoneNumber 10-digit mobile number (e.g. 9876543210)
     * @param otpCode     6-digit numeric OTP (e.g. 592814)
     * @return true if dispatched or simulated successfully
     */
    public boolean sendOtpSms(String phoneNumber, String otpCode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || otpCode == null) {
            logger.warn("SMS dispatch aborted: missing phone number or OTP code.");
            return false;
        }

        String cleanPhone = phoneNumber.trim().replaceAll("[^0-9]", "");
        String message = "Your ShopKart password reset verification code is: " + otpCode + 
                         ". Valid for 10 minutes. Do not share this OTP with anyone.";

        // 1. Check for Fast2SMS (Indian Gateway)
        if (fast2smsApiKey != null && !fast2smsApiKey.trim().isEmpty()) {
            return sendViaFast2SMS(cleanPhone, message);
        }

        // 2. Check for Twilio (Global Gateway)
        if (twilioSid != null && twilioToken != null && twilioFrom != null) {
            return sendViaTwilio(cleanPhone, message);
        }

        // 3. Simulated Sandbox Fallback
        logger.info("══════════════════════════════════════════════════════════════");
        logger.info("📱 [SMS SIMULATOR] Dispatching SMS to +91-{}", cleanPhone);
        logger.info("📩 Message: {}", message);
        logger.info("🔑 OTP Code: {}", otpCode);
        logger.info("══════════════════════════════════════════════════════════════");
        return true;
    }

    private boolean sendViaFast2SMS(String phone, String message) {
        try {
            String url = "https://www.fast2sms.com/dev/bulkV2?route=q&message=" + 
                         URLEncoder.encode(message, StandardCharsets.UTF_8) +
                         "&language=english&flash=0&numbers=" + URLEncoder.encode(phone, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("authorization", fast2smsApiKey.trim())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("SMS successfully sent via Fast2SMS to {}", phone);
                return true;
            } else {
                logger.warn("Fast2SMS gateway returned status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to send SMS via Fast2SMS to {}", phone, e);
            return false;
        }
    }

    private boolean sendViaTwilio(String phone, String message) {
        try {
            String toPhone = phone.startsWith("+") ? phone : "+91" + phone;
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioSid + "/Messages.json";
            String formData = "To=" + URLEncoder.encode(toPhone, StandardCharsets.UTF_8) +
                              "&From=" + URLEncoder.encode(twilioFrom, StandardCharsets.UTF_8) +
                              "&Body=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            String auth = java.util.Base64.getEncoder().encodeToString((twilioSid + ":" + twilioToken).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("SMS successfully sent via Twilio to {}", toPhone);
                return true;
            } else {
                logger.warn("Twilio gateway returned status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to send SMS via Twilio to {}", phone, e);
            return false;
        }
    }

    private String getEnv(String name, String fallback) {
        String val = System.getenv(name);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }
}
