package com.example.ecommerce.util;

import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.model.OrderStatus;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for sending transactional emails (Password Resets, OTP Verification,
 * Order Invoices)
 * via Brevo/Resend HTTPS REST API (Port 443) or fallback SMTP.
 */
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final String SMTP_HOST = "smtp.gmail.com";

    private static final ExecutorService mailExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ShopKart-Mail-Worker");
        t.setDaemon(true);
        return t;
    });

    private final String smtpEmail;
    private final String smtpPassword;
    private final String fromName;
    private final String appBaseUrl;

    public EmailService() {
        String envEmail = System.getenv("SMTP_EMAIL");
        String envPass = System.getenv("SMTP_PASSWORD");
        String envFromName = System.getenv("SMTP_FROM_NAME");
        String envBaseUrl = System.getenv("APP_BASE_URL");

        this.smtpEmail = (envEmail != null) ? envEmail.trim() : "";
        this.smtpPassword = (envPass != null) ? envPass.replaceAll("\\s+", "").trim() : "";
        this.fromName = (envFromName != null && !envFromName.trim().isEmpty()) ? envFromName.trim()
                : "ShopKart Support";
        this.appBaseUrl = (envBaseUrl != null && !envBaseUrl.trim().isEmpty())
                ? envBaseUrl.replaceAll("/+$", "")
                : "https://shopkart-ecommerce-1m2n.onrender.com";
    }

    /**
     * Sends a 6-digit OTP email for secure Password Reset.
     */
    public void sendPasswordResetOtp(String toEmail, String toName, String otpCode) {
        mailExecutor.submit(() -> {
            try {
                String brevoApiKey = System.getenv("BREVO_API_KEY");
                String resendApiKey = System.getenv("RESEND_API_KEY");
                String htmlBody = buildPasswordResetOtpBody(toName, otpCode);
                String subject = otpCode + " is your ShopKart Password Reset Code";

                if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
                    sendViaBrevoHttps(brevoApiKey.trim(), toEmail, toName, subject, htmlBody);
                    return;
                }
                if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                    sendViaResendHttps(resendApiKey.trim(), toEmail, subject, htmlBody);
                    return;
                }

                if (smtpEmail.isEmpty() || smtpPassword.isEmpty()) {
                    logger.info("🔑 Password reset OTP for {}: {}", toEmail, otpCode);
                    return;
                }

                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Password reset OTP email sent to: {}", toEmail);

            } catch (Exception e) {
                logger.warn("Non-fatal: SMTP delivery failed for password reset OTP to: {}. Error: {}", toEmail,
                        e.getMessage());
            }
        });
    }

    /**
     * Sends a forgot-password email with the secure reset link (legacy fallback).
     */
    public void sendPasswordResetEmail(String toEmail, String toName, String resetLink) {
        mailExecutor.submit(() -> {
            try {
                String brevoApiKey = System.getenv("BREVO_API_KEY");
                String resendApiKey = System.getenv("RESEND_API_KEY");
                String htmlBody = buildHtmlBody(toName, resetLink);
                String subject = "Reset Your ShopKart Password";

                if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
                    sendViaBrevoHttps(brevoApiKey.trim(), toEmail, toName, subject, htmlBody);
                    return;
                }
                if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                    sendViaResendHttps(resendApiKey.trim(), toEmail, subject, htmlBody);
                    return;
                }

                if (smtpEmail.isEmpty() || smtpPassword.isEmpty()) {
                    logger.info("🔑 Dev Mode: Password reset link for {}: {}", toEmail, resetLink);
                    return;
                }

                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Password reset email sent to: {}", toEmail);

            } catch (Exception e) {
                logger.warn("Non-fatal: SMTP delivery failed for password reset to: {}. Error: {}", toEmail,
                        e.getMessage());
            }
        });
    }

    /**
     * Sends a 6-digit OTP email for user registration / email verification.
     */
    public void sendSignupVerificationOtp(String toEmail, String toName, String otpCode) {
        mailExecutor.submit(() -> {
            try {
                String brevoApiKey = System.getenv("BREVO_API_KEY");
                String resendApiKey = System.getenv("RESEND_API_KEY");
                String htmlBody = buildOtpBody(toName, otpCode);
                String subject = otpCode + " is your ShopKart Verification Code";

                if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
                    sendViaBrevoHttps(brevoApiKey.trim(), toEmail, toName, subject, htmlBody);
                    return;
                }
                if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                    sendViaResendHttps(resendApiKey.trim(), toEmail, subject, htmlBody);
                    return;
                }

                if (smtpEmail.isEmpty() || smtpPassword.isEmpty()) {
                    logger.info("🔑 Dev Mode: Signup verification OTP for {}: {}", toEmail, otpCode);
                    return;
                }

                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Signup verification OTP email sent to: {}", toEmail);

            } catch (Exception e) {
                logger.warn("Non-fatal: SMTP delivery failed for OTP to: {}. Error: {}", toEmail, e.getMessage());
            }
        });
    }

    /**
     * Sends an Order Status update / Confirmation Invoice email.
     */
    public void sendOrderStatusUpdateEmail(String toEmail, String toName, Order order, OrderStatus newStatus,
            String remarks) {
        if (order == null || toEmail == null || toEmail.trim().isEmpty()) {
            return;
        }

        mailExecutor.submit(() -> {
            try {
                String brevoApiKey = System.getenv("BREVO_API_KEY");
                String resendApiKey = System.getenv("RESEND_API_KEY");
                String subject = buildOrderStatusSubject(order.getOrderNumber(), newStatus, order.getCourierPartner(),
                        order.getTrackingNumber());
                String htmlBody = buildOrderStatusEmailHtml(toName, order, newStatus, remarks);

                if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
                    sendViaBrevoHttps(brevoApiKey.trim(), toEmail, toName, subject, htmlBody);
                    return;
                }
                if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                    sendViaResendHttps(resendApiKey.trim(), toEmail, subject, htmlBody);
                    return;
                }

                if (smtpEmail.isEmpty() || smtpPassword.isEmpty()) {
                    logger.info("Dev Mode: Order status [{}] update for Order #{}: Total ₹{}",
                            newStatus, order.getOrderNumber(), order.getTotalAmount());
                    return;
                }

                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Order status [{}] update email sent to: {} for Order #{}", newStatus, toEmail,
                        order.getOrderNumber());

            } catch (Exception e) {
                logger.warn("Non-fatal: Email delivery failed for order #{}: {}", order.getOrderNumber(),
                        e.getMessage());
            }
        });
    }

    private void sendViaBrevoHttps(String apiKey, String toEmail, String toName, String subject, String htmlContent) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonPayload = String.format(
                    "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}],\"subject\":\"%s\",\"htmlContent\":%s}",
                    fromName,
                    (!smtpEmail.isEmpty() ? smtpEmail : "shopkart.support@gmail.com"),
                    escapeJson(toEmail),
                    toName != null ? escapeJson(toName.replace("\"", "")) : "\"\"",
                    subject.replace("\"", ""),
                    escapeJson(htmlContent));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                logger.info("Email delivered via Brevo HTTPS API to: {}", toEmail);
            } else {
                logger.warn("Brevo API returned status {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            logger.warn("Brevo HTTPS API delivery error: {}", e.getMessage());
        }
    }

    private void sendViaResendHttps(String apiKey, String toEmail, String subject, String htmlContent) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonPayload = String.format(
                    "{\"from\":\"%s <%s>\",\"to\":[\"%s\"],\"subject\":\"%s\",\"html\":%s}",
                    fromName,
                    (!smtpEmail.isEmpty() ? smtpEmail : "onboarding@resend.dev"),
                    escapeJson(toEmail),
                    subject.replace("\"", ""),
                    escapeJson(htmlContent));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                logger.info("Email delivered via Resend HTTPS API to: {}", toEmail);
            } else {
                logger.warn("Resend API returned status {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            logger.warn("Resend HTTPS API delivery error: {}", e.getMessage());
        }
    }

    private Session buildMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpEmail, smtpPassword);
            }
        });
    }

    private String buildOrderStatusSubject(String orderNumber, OrderStatus status, String courierPartner,
            String trackingNumber) {
        if (status == null)
            return "Order #" + orderNumber + " Update | ShopKart";
        switch (status) {
            case CONFIRMED:
            case PENDING:
                return "🛒 Order Confirmed: #" + orderNumber + " | ShopKart";
            case PROCESSING:
                return "📦 Order #" + orderNumber + " is Packed & Preparing for Dispatch | ShopKart";
            case DISPATCHED:
                return "🚚 Order #" + orderNumber + " Dispatched" +
                        (courierPartner != null ? " via " + courierPartner : "") + " | ShopKart";
            case DELIVERED:
                return "🎉 Delivered: Your ShopKart Order #" + orderNumber + " has arrived!";
            case CANCELLED:
                return "✕ Order #" + orderNumber + " Cancelled | ShopKart";
            default:
                return "📦 Order #" + orderNumber + " Status Update: " + status.getDisplayName() + " | ShopKart";
        }
    }

    private String buildOrderStatusEmailHtml(String name, Order order, OrderStatus newStatus, String remarks) {
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemsHtml.append("<tr>")
                        .append("<td style='padding:8px 0; font-size:13px; color:#1e293b;'>")
                        .append(escapeHtml(item.getProductName())).append("</td>")
                        .append("<td align='center' style='padding:8px 0; font-size:13px; color:#64748b;'>")
                        .append(item.getQuantity()).append("</td>")
                        .append("<td align='right' style='padding:8px 0; font-size:13px; font-weight:700; color:#0f172a;'>₹")
                        .append(item.getLineTotal()).append("</td>")
                        .append("</tr>");
            }
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,sans-serif; background:#f8fafc; margin:0; padding:20px;'>"
                +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center'>" +
                "<table width='580' cellpadding='0' cellspacing='0' style='background:#fff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                +
                "<tr><td style='background:#0f172a; padding:24px 32px; text-align:center;'><h1 style='color:#f59e0b; font-size:24px; margin:0;'>🛒 ShopKart</h1><p style='color:#94a3b8; font-size:12px; margin:4px 0 0;'>Order Confirmation & Invoice</p></td></tr>"
                +
                "<tr><td style='padding:28px 32px;'>" +
                "<h2 style='font-size:18px; color:#0f172a; margin:0 0 8px;'>Order #"
                + escapeHtml(order.getOrderNumber()) + "</h2>" +
                "<p style='color:#475569; font-size:14px; line-height:1.5; margin:0 0 20px;'>Hi "
                + escapeHtml(name != null ? name : "Customer")
                + ", thank you for your purchase! Your order has been placed successfully and is now <strong>"
                + (newStatus != null ? newStatus.getDisplayName() : "Processing") + "</strong>.</p>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='border-top:1px solid #e2e8f0; border-bottom:1px solid #e2e8f0; margin-bottom:16px;'>"
                +
                "<tr style='color:#64748b; font-size:11px; text-transform:uppercase; font-weight:700;'><th align='left' style='padding:8px 0;'>Item</th><th align='center' style='padding:8px 0;'>Qty</th><th align='right' style='padding:8px 0;'>Price</th></tr>"
                +
                itemsHtml.toString() +
                "</table>" +
                "<table width='100%' cellpadding='0' cellspacing='0'>" +
                "<tr><td align='right' style='font-size:14px; font-weight:700; color:#0f172a;'>Total Amount: <span style='color:#2563eb;'>₹"
                + order.getTotalAmount() + "</span></td></tr>" +
                "</table>" +
                "<div style='text-align:center; margin-top:24px;'><a href='" + appBaseUrl
                + "/orders' style='background:#f59e0b; color:#0f172a; text-decoration:none; font-weight:800; font-size:13px; padding:10px 24px; border-radius:6px; display:inline-block;'>View Your Order Online →</a></div>"
                +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private String buildOtpBody(String name, String otpCode) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,sans-serif; background:#f8fafc; margin:0; padding:20px;'>"
                +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center'>" +
                "<table width='500' cellpadding='0' cellspacing='0' style='background:#fff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                +
                "<tr><td style='background:#0f172a; padding:24px 32px; text-align:center;'><h1 style='color:#f59e0b; font-size:24px; margin:0;'>🛒 ShopKart</h1></td></tr>"
                +
                "<tr><td style='padding:28px 32px; text-align:center;'>" +
                "<h2 style='font-size:20px; color:#0f172a; margin:0 0 12px;'>Email Verification Code</h2>" +
                "<p style='color:#475569; font-size:14px; margin:0 0 20px;'>Hi " + escapeHtml(name)
                + ", use the code below to verify your ShopKart account:</p>" +
                "<div style='display:inline-block; background:#eff6ff; border:2px dashed #3b82f6; border-radius:8px; padding:12px 28px; font-size:28px; font-weight:900; letter-spacing:8px; color:#1d4ed8; font-family:monospace; margin-bottom:20px;'>"
                + otpCode + "</div>" +
                "<p style='color:#94a3b8; font-size:12px; margin:0;'>This code expires in 10 minutes. If you did not request this code, please ignore this email.</p>"
                +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private String buildPasswordResetOtpBody(String name, String otpCode) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,sans-serif; background:#f8fafc; margin:0; padding:20px;'>"
                +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center'>" +
                "<table width='500' cellpadding='0' cellspacing='0' style='background:#fff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                +
                "<tr><td style='background:#0f172a; padding:24px 32px; text-align:center;'><h1 style='color:#f59e0b; font-size:24px; margin:0;'>🛒 ShopKart</h1></td></tr>"
                +
                "<tr><td style='padding:28px 32px; text-align:center;'>" +
                "<h2 style='font-size:20px; color:#0f172a; margin:0 0 12px;'>Password Reset Verification Code</h2>" +
                "<p style='color:#475569; font-size:14px; margin:0 0 20px;'>Hi " + (name != null ? escapeHtml(name) : "Customer")
                + ", use the 6-digit verification code below to reset your ShopKart account password:</p>" +
                "<div style='display:inline-block; background:#fef3c7; border:2px dashed #f59e0b; border-radius:8px; padding:12px 28px; font-size:30px; font-weight:900; letter-spacing:8px; color:#b45309; font-family:monospace; margin-bottom:20px;'>"
                + otpCode + "</div>" +
                "<p style='color:#64748b; font-size:13px; margin:0 0 8px;'>This code is valid for <strong>10 minutes</strong>.</p>" +
                "<p style='color:#94a3b8; font-size:12px; margin:0;'>If you did not request a password reset, please ignore this email or secure your account.</p>" +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private String buildHtmlBody(String name, String resetLink) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,sans-serif; background:#f8fafc; margin:0; padding:20px;'>"
                +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center'>" +
                "<table width='500' cellpadding='0' cellspacing='0' style='background:#fff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.05);'>"
                +
                "<tr><td style='background:#0f172a; padding:24px 32px; text-align:center;'><h1 style='color:#f59e0b; font-size:24px; margin:0;'>🛒 ShopKart</h1></td></tr>"
                +
                "<tr><td style='padding:28px 32px; text-align:center;'>" +
                "<h2 style='font-size:20px; color:#0f172a; margin:0 0 12px;'>Password Reset Request</h2>" +
                "<p style='color:#475569; font-size:14px; margin:0 0 20px;'>Hi " + escapeHtml(name)
                + ", click the button below to reset your ShopKart password:</p>" +
                "<a href='" + resetLink.replace("&", "&amp;")
                + "' style='background:#f59e0b; color:#0f172a; text-decoration:none; font-weight:800; font-size:14px; padding:12px 28px; border-radius:6px; display:inline-block; margin-bottom:20px;'>Reset My Password →</a>"
                +
                "<p style='color:#94a3b8; font-size:12px; margin:0;'>This link expires in 1 hour.</p>" +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private String escapeJson(String str) {
        if (str == null)
            return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String escapeHtml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}