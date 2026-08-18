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

import java.math.BigDecimal;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Email Service using Jakarta Mail (Jakarta EE compatible) with Gmail SMTP.
 *
 * <p>
 * Configuration is read exclusively from environment variables so no
 * credentials
 * are ever committed to source control:
 * </p>
 * <ul>
 * <li>{@code SMTP_EMAIL} — the Gmail address (e.g. yourstore@gmail.com)</li>
 * <li>{@code SMTP_PASSWORD} — the Gmail App Password (16-char, no spaces)</li>
 * <li>{@code SMTP_FROM_NAME} — (optional) display name, defaults to
 * "ShopKart"</li>
 * </ul>
 */
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 465;

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
        String email = System.getenv("SMTP_EMAIL");
        this.smtpEmail = (email != null) ? email.trim() : null;
        String pass = System.getenv("SMTP_PASSWORD");
        this.smtpPassword = (pass != null) ? pass.trim().replaceAll("\\s+", "") : null;
        String name = System.getenv("SMTP_FROM_NAME");
        this.fromName = (name != null && !name.trim().isEmpty()) ? name.trim() : "ShopKart";
        String baseUrl = System.getenv("APP_BASE_URL");
        this.appBaseUrl = (baseUrl != null && !baseUrl.trim().isEmpty()) 
                ? baseUrl.trim().replaceAll("/+$", "") 
                : "https://shopkart-ecommerce-1m2n.onrender.com";
    }

    /**
     * Sends a forgot-password email with the secure reset link.
     *
     * @param toEmail   recipient email address
     * @param toName    recipient display name
     * @param resetLink full reset URL (e.g.
     *                  https://yourapp.com/reset-password?token=...)
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

                if (smtpEmail == null || smtpEmail.trim().isEmpty() ||
                        smtpPassword == null || smtpPassword.trim().isEmpty()) {
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
                logger.warn("Non-fatal: SMTP delivery failed for password reset to: {}. Error: {}", toEmail, e.getMessage());
            }
        });
    }

    /**
     * Sends a 6-digit OTP email for user registration / email verification.
     * Runs asynchronously in background to ensure zero UI latency.
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

                if (smtpEmail == null || smtpEmail.trim().isEmpty() ||
                        smtpPassword == null || smtpPassword.trim().isEmpty()) {
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

    private void sendViaBrevoHttps(String apiKey, String toEmail, String toName, String subject, String htmlContent) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String jsonPayload = String.format(
                    "{\"sender\":{\"name\":\"%s\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}],\"subject\":\"%s\",\"htmlContent\":%s}",
                    fromName,
                    (smtpEmail != null && !smtpEmail.isEmpty() ? smtpEmail : "no-reply@shopkart.eu.org"),
                    toEmail,
                    toName != null ? toName.replace("\"", "") : "",
                    subject.replace("\"", ""),
                    escapeJson(htmlContent)
            );

            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
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
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String jsonPayload = String.format(
                    "{\"from\":\"%s <%s>\",\"to\":[\"%s\"],\"subject\":\"%s\",\"html\":%s}",
                    fromName,
                    (smtpEmail != null && !smtpEmail.isEmpty() ? smtpEmail : "onboarding@resend.dev"),
                    toEmail,
                    subject.replace("\"", ""),
                    escapeJson(htmlContent)
            );

            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                logger.info("Email delivered via Resend HTTPS API to: {}", toEmail);
            } else {
                logger.warn("Resend API returned status {}: {}", resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            logger.warn("Resend HTTPS API delivery error: {}", e.getMessage());
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "\"\"";
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

    private String buildHtmlBody(String name, String resetLink) {
        String safeLink = resetLink.replace("&", "&amp;");
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif; " +
                "background:#f8fafc; margin:0; padding:0;'>" +
                "  <table width='100%' cellpadding='0' cellspacing='0'>" +
                "    <tr><td align='center' style='padding:40px 20px;'>" +
                "      <table width='560' cellpadding='0' cellspacing='0' " +
                "             style='background:#fff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden;'>"
                +
                // Header
                "        <tr><td style='background:#0f172a; padding:28px 40px; text-align:center;'>" +
                "          <h1 style='color:#f59e0b; font-size:26px; margin:0; letter-spacing:-0.5px;'>🛒 ShopKart</h1>"
                +
                "          <p style='color:#94a3b8; font-size:13px; margin:8px 0 0;'>India's Premier Online Store</p>" +
                "        </td></tr>" +
                // Body
                "        <tr><td style='padding:36px 40px;'>" +
                "          <h2 style='font-size:20px; color:#111827; margin:0 0 12px;'>Reset Your Password</h2>" +
                "          <p style='color:#4b5563; font-size:15px; line-height:1.6; margin:0 0 24px;'>" +
                "            Hi " + escapeHtml(name) + ", we received a request to reset your ShopKart password. " +
                "            Click the button below to create a new password. This link expires in <strong>1 hour</strong>."
                +
                "          </p>" +
                "          <div style='text-align:center; margin:32px 0;'>" +
                "            <a href='" + safeLink + "' " +
                "               style='background:#f59e0b; color:#111827; text-decoration:none; font-weight:800; " +
                "                      font-size:15px; padding:14px 36px; border-radius:8px; display:inline-block;'>" +
                "              Reset Password →" +
                "            </a>" +
                "          </div>" +
                "          <p style='color:#6b7280; font-size:13px; line-height:1.5; margin:24px 0 0;'>" +
                "            If you didn't request this, you can safely ignore this email. Your password won't change.<br>"
                +
                "            Or copy this link: <span style='color:#2563eb; word-break:break-all;'>" + safeLink
                + "</span>" +
                "          </p>" +
                "        </td></tr>" +
                // Footer
                "        <tr><td style='background:#f8fafc; border-top:1px solid #e5e7eb; padding:20px 40px; text-align:center;'>"
                +
                "          <p style='color:#9ca3af; font-size:12px; margin:0;'>" +
                "            &copy; 2026 ShopKart India Inc. &bull; This email was sent to " + escapeHtml(name) +
                "          </p>" +
                "        </td></tr>" +
                "      </table>" +
                "    </td></tr>" +
                "  </table>" +
                "</body></html>";
    }

    private String buildOtpBody(String name, String otpCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif; " +
                "background:#f8fafc; margin:0; padding:0;'>" +
                "  <table width='100%' cellpadding='0' cellspacing='0'>" +
                "    <tr><td align='center' style='padding:40px 20px;'>" +
                "      <table width='540' cellpadding='0' cellspacing='0' " +
                "             style='background:#ffffff; border-radius:12px; box-shadow:0 4px 24px rgba(0,0,0,0.08); overflow:hidden; border:1px solid #e2e8f0;'>"
                +
                // Header
                "        <tr><td style='background:#0f172a; padding:28px 36px; text-align:center;'>" +
                "          <h1 style='color:#f59e0b; font-size:24px; margin:0; letter-spacing:-0.5px;'>🛒 ShopKart</h1>"
                +
                "          <p style='color:#94a3b8; font-size:13px; margin:6px 0 0;'>India's Premier Online Store</p>" +
                "        </td></tr>" +
                // Body
                "        <tr><td style='padding:36px; text-align:center;'>" +
                "          <div style='font-size:38px; margin-bottom:12px;'>✉️</div>" +
                "          <h2 style='font-size:20px; font-weight:800; color:#111827; margin:0 0 10px;'>Verify Your Email Address</h2>"
                +
                "          <p style='color:#4b5563; font-size:14px; line-height:1.6; margin:0 0 24px; text-align:center;'>"
                +
                "            Hi <strong>" + escapeHtml(name) + "</strong>, thank you for signing up on ShopKart. " +
                "            Please use the following 6-digit verification code to complete your registration and activate your account:"
                +
                "          </p>" +
                "          <div style='display:inline-block; background:#f1f5f9; border:2px dashed #cbd5e1; border-radius:10px; padding:16px 36px; margin:16px 0;'>"
                +
                "            <span style='font-size:32px; font-weight:900; letter-spacing:8px; color:#0f172a; font-family:monospace;'>"
                + otpCode + "</span>" +
                "          </div>" +
                "          <p style='color:#6b7280; font-size:12px; margin:16px 0 0;'>" +
                "            ⏱️ This verification code is valid for <strong>10 minutes</strong>. Do not share this code with anyone."
                +
                "          </p>" +
                "        </td></tr>" +
                // Footer
                "        <tr><td style='background:#f8fafc; border-top:1px solid #e5e7eb; padding:16px 36px; text-align:center;'>"
                +
                "          <p style='color:#9ca3af; font-size:12px; margin:0;'>" +
                "            &copy; 2026 ShopKart India Inc. &bull; 100% Purchase Protection &bull; Safe & Secure" +
                "          </p>" +
                "        </td></tr>" +
                "      </table>" +
                "    </td></tr>" +
                "  </table>" +
                "</body></html>";
    }

    /**
     * Sends an HTML order status transition notification email to the customer.
     *
     * @param toEmail      Customer email address
     * @param toName       Customer display name
     * @param order        Order domain object containing items, totals, shipping info, and logistics
     * @param newStatus    The newly updated OrderStatus stage
     * @param remarks      Logistics / admin remarks note
     */
    public void sendOrderStatusUpdateEmail(String toEmail, String toName, Order order, OrderStatus newStatus, String remarks) {
        if (toEmail == null || toEmail.trim().isEmpty() || order == null) {
            return;
        }

        if (smtpEmail == null || smtpEmail.trim().isEmpty() ||
                smtpPassword == null || smtpPassword.trim().isEmpty()) {
            logger.info("SMTP credentials not configured (SMTP_EMAIL/SMTP_PASSWORD). " +
                    "Order update notification email logged for Order #{}: Stage: [{}], Recipient: <{}>, Remarks: {}",
                    order.getOrderNumber(), newStatus, toEmail, remarks);
            return;
        }

        mailExecutor.submit(() -> {
            try {
                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

                String subject = getOrderStatusEmailSubject(order.getOrderNumber(), newStatus, order.getCourierPartner(), order.getTrackingNumber());
                message.setSubject(subject, "UTF-8");
                message.setContent(buildOrderStatusEmailHtml(toName != null ? toName : "Valued Customer", order, newStatus, remarks), "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Order status update [{}] email successfully sent to: {} for Order #{}",
                        newStatus, toEmail, order.getOrderNumber());
            } catch (Exception e) {
                logger.error("Failed to send order status [{}] email to: {} for Order #{}",
                        newStatus, toEmail, order.getOrderNumber(), e);
            }
        });
    }

    private String getOrderStatusEmailSubject(String orderNumber, OrderStatus status, String courierPartner, String trackingNumber) {
        if (status == null) {
            return "Order #" + orderNumber + " Update | ShopKart";
        }
        switch (status) {
            case CONFIRMED:
            case PENDING:
                return "🛒 Order Confirmed: #" + orderNumber + " | ShopKart";
            case PROCESSING:
                return "📦 Order #" + orderNumber + " is Packed & Being Prepared for Dispatch | ShopKart";
            case DISPATCHED:
                String carrierInfo = (courierPartner != null && !courierPartner.isEmpty()) ? " via " + courierPartner : "";
                String awbInfo = (trackingNumber != null && !trackingNumber.isEmpty()) ? " [AWB: " + trackingNumber + "]" : "";
                return "🚚 Order #" + orderNumber + " Dispatched" + carrierInfo + awbInfo + " | ShopKart";
            case IN_TRANSIT:
            case SHIPPED:
                return "✈️ Shipment Update: Order #" + orderNumber + " is In Transit | ShopKart";
            case OUT_FOR_DELIVERY:
                return "🛵 Out for Delivery: Order #" + orderNumber + " will arrive today! | ShopKart";
            case DELIVERED:
                return "🎉 Delivered: Your ShopKart Order #" + orderNumber + " has arrived!";
            case CANCELLED:
                return "✕ Order #" + orderNumber + " Cancelled | ShopKart";
            case RETURN_REQUESTED:
                return "↩️ Return Request Received for Order #" + orderNumber + " | ShopKart";
            case RETURNED:
                return "🔄 Return Received & Processed for Order #" + orderNumber + " | ShopKart";
            default:
                return "📦 Order #" + orderNumber + " Status Update: " + status.getDisplayName() + " | ShopKart";
        }
    }

    private String buildOrderStatusEmailHtml(String name, Order order, OrderStatus newStatus, String remarks) {
        String statusColor = "#3b82f6";
        String statusIcon = "📦";
        String statusHeadline = "Order Status Update";
        String statusMessage = "Your order is progressing smoothly through our fulfillment network.";

        if (newStatus != null) {
            switch (newStatus) {
                case CONFIRMED:
                case PENDING:
                    statusColor = "#0284c7";
                    statusIcon = "🛒";
                    statusHeadline = "Order Placed & Confirmed!";
                    statusMessage = "Thank you for shopping with ShopKart! We have received your order and our fulfillment team is reviewing it.";
                    break;
                case PROCESSING:
                    statusColor = "#6366f1";
                    statusIcon = "📦";
                    statusHeadline = "Order Prepared for Dispatch!";
                    statusMessage = "Your items have been prepared and packed securely for dispatch from our fulfillment center.";
                    break;
                case DISPATCHED:
                    statusColor = "#2563eb";
                    statusIcon = "🚚";
                    statusHeadline = "Handed Over to Courier!";
                    statusMessage = "Your package has been dispatched from our regional hub and is on its way to your destination.";
                    break;
                case IN_TRANSIT:
                case SHIPPED:
                    statusColor = "#3b82f6";
                    statusIcon = "✈️";
                    statusHeadline = "Shipment In Transit!";
                    statusMessage = "Your package is currently in transit between logistics sort centers.";
                    break;
                case OUT_FOR_DELIVERY:
                    statusColor = "#f59e0b";
                    statusIcon = "🛵";
                    statusHeadline = "Out for Doorstep Delivery!";
                    statusMessage = "Great news! Your package has arrived at the local delivery center and is with the delivery agent.";
                    break;
                case DELIVERED:
                    statusColor = "#10b981";
                    statusIcon = "🎁";
                    statusHeadline = "Delivered Successfully!";
                    statusMessage = "Your package has been successfully handed over to you. We hope you enjoy your purchase!";
                    break;
                case CANCELLED:
                    statusColor = "#ef4444";
                    statusIcon = "✕";
                    statusHeadline = "Order Cancelled";
                    statusMessage = "Your order has been cancelled. Any reserved stock and payments have been released.";
                    break;
                case RETURN_REQUESTED:
                    statusColor = "#8b5cf6";
                    statusIcon = "↩️";
                    statusHeadline = "Return Request Received";
                    statusMessage = "We have received your return request. Our logistics agent will reach out for reverse pickup.";
                    break;
                case RETURNED:
                    statusColor = "#10b981";
                    statusIcon = "🔄";
                    statusHeadline = "Return Processed & Restocked";
                    statusMessage = "The returned items have reached our fulfillment center and return inspection is complete.";
                    break;
            }
        }

        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                itemsHtml.append("<tr>")
                        .append("<td style='padding:10px 12px; border-bottom:1px solid #f1f5f9;'>")
                        .append("  <div style='font-weight:700; color:#0f172a; font-size:13px;'>").append(escapeHtml(item.getProductName())).append("</div>")
                        .append("  <div style='font-size:11px; color:#64748b;'>SKU: ").append(escapeHtml(item.getSku())).append("</div>")
                        .append("</td>")
                        .append("<td style='padding:10px 12px; text-align:center; border-bottom:1px solid #f1f5f9; font-weight:700; color:#334155;'>")
                        .append(item.getQuantity())
                        .append("</td>")
                        .append("<td style='padding:10px 12px; text-align:right; border-bottom:1px solid #f1f5f9; font-weight:800; color:#0f172a;'>")
                        .append("₹").append(item.getLineTotal() != null ? item.getLineTotal().setScale(2, java.math.RoundingMode.HALF_UP) : "0.00")
                        .append("</td>")
                        .append("</tr>");
            }
        }

        StringBuilder logisticsBox = new StringBuilder();
        if (order.getCourierPartner() != null || order.getTrackingNumber() != null || order.getDeliveryAgentPhone() != null) {
            logisticsBox.append("<div style='background:#f0fdf4; border:1px solid #bbf7d0; border-radius:8px; padding:14px 18px; margin:20px 0;'>")
                    .append("<div style='font-weight:800; color:#166534; font-size:14px; margin-bottom:4px;'>🚚 Logistics & Tracking Details</div>");
            
            if (order.getCourierPartner() != null && !order.getCourierPartner().isEmpty()) {
                logisticsBox.append("<div style='font-size:13px; color:#15803d; margin-top:2px;'>Courier Partner: <strong>")
                        .append(escapeHtml(order.getCourierPartner())).append("</strong></div>");
            }
            if (order.getTrackingNumber() != null && !order.getTrackingNumber().isEmpty()) {
                logisticsBox.append("<div style='font-size:13px; color:#15803d; margin-top:2px;'>AWB / Tracking Number: <strong style='font-family:monospace; background:#fff; padding:2px 6px; border-radius:4px; border:1px dashed #86efac;'>")
                        .append(escapeHtml(order.getTrackingNumber())).append("</strong></div>");
            }
            if (order.getDeliveryAgentPhone() != null && !order.getDeliveryAgentPhone().isEmpty()) {
                logisticsBox.append("<div style='font-size:13px; color:#15803d; margin-top:2px;'>Delivery Agent Phone: <strong>")
                        .append(escapeHtml(order.getDeliveryAgentPhone())).append("</strong></div>");
            }
            logisticsBox.append("</div>");
        }

        String remarksBox = "";
        if (remarks != null && !remarks.trim().isEmpty()) {
            remarksBox = "<div style='background:#f8fafc; border-left:4px solid " + statusColor + "; padding:10px 14px; border-radius:4px; margin:16px 0; font-size:13px; color:#334155;'>" +
                    "<strong>Update Note:</strong> " + escapeHtml(remarks.trim()) + "</div>";
        }

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family:-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif; background:#f1f5f9; margin:0; padding:0;'>" +
                "  <table width='100%' cellpadding='0' cellspacing='0'>" +
                "    <tr><td align='center' style='padding:30px 15px;'>" +
                "      <table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff; border-radius:12px; box-shadow:0 6px 24px rgba(0,0,0,0.06); overflow:hidden; border:1px solid #e2e8f0;'>" +
                // Brand Header
                "        <tr><td style='background:#0f172a; padding:24px 32px; text-align:center;'>" +
                "          <h1 style='color:#f59e0b; font-size:24px; margin:0; letter-spacing:-0.5px;'>🛒 ShopKart</h1>" +
                "          <p style='color:#94a3b8; font-size:12px; margin:4px 0 0;'>India's Premier Online Store &bull; Fast Doorstep Delivery</p>" +
                "        </td></tr>" +
                // Status Highlight Banner
                "        <tr><td style='background:" + statusColor + "; padding:16px 32px; text-align:center; color:#ffffff;'>" +
                "          <div style='font-size:30px; line-height:1; margin-bottom:4px;'>" + statusIcon + "</div>" +
                "          <h2 style='font-size:18px; font-weight:800; margin:0;'>" + statusHeadline + "</h2>" +
                "        </td></tr>" +
                // Body Content
                "        <tr><td style='padding:28px 32px;'>" +
                "          <p style='color:#334155; font-size:14px; line-height:1.6; margin:0 0 16px;'>" +
                "            Hi <strong>" + escapeHtml(name) + "</strong>,<br>" +
                "            " + statusMessage +
                "          </p>" +
                "          <div style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px 16px; margin:16px 0; display:flex; justify-content:space-between;'>" +
                "            <span style='font-size:13px; color:#64748b;'>Order #: <strong style='color:#0f172a; font-size:14px;'>" + escapeHtml(order.getOrderNumber()) + "</strong></span>" +
                "            <span style='font-size:13px; color:#64748b;'>Status: <strong style='color:" + statusColor + "; font-size:14px;'>" + (newStatus != null ? newStatus.name() : "UPDATED") + "</strong></span>" +
                "          </div>" +
                remarksBox +
                logisticsBox.toString() +
                // Items Table
                "          <div style='margin-top:20px;'>" +
                "            <h3 style='font-size:14px; font-weight:800; color:#0f172a; margin:0 0 10px; text-transform:uppercase; letter-spacing:0.05em;'>Items in this Order</h3>" +
                "            <table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #f1f5f9; border-radius:6px; overflow:hidden;'>" +
                "              <thead>" +
                "                <tr style='background:#f8fafc; color:#64748b; font-size:11px; text-transform:uppercase;'>" +
                "                  <th align='left' style='padding:8px 12px;'>Product</th>" +
                "                  <th align='center' style='padding:8px 12px;'>Qty</th>" +
                "                  <th align='right' style='padding:8px 12px;'>Total</th>" +
                "                </tr>" +
                "              </thead>" +
                "              <tbody>" +
                itemsHtml.toString() +
                "              </tbody>" +
                "            </table>" +
                "          </div>" +
                // Shipping & Price Breakdown
                "          <table width='100%' cellpadding='0' cellspacing='0' style='margin-top:20px;'>" +
                "            <tr>" +
                "              <td width='50%' valign='top' style='padding-right:10px;'>" +
                "                <div style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px;'>" +
                "                  <div style='font-size:11px; font-weight:800; color:#64748b; text-transform:uppercase;'>📍 Shipping Address</div>" +
                "                  <div style='font-size:12px; color:#0f172a; font-weight:700; margin-top:4px;'>" + escapeHtml(order.getShippingFullName()) + "</div>" +
                "                  <div style='font-size:12px; color:#475569; line-height:1.4;'>" +
                escapeHtml(order.getShippingAddressLine1()) + "<br>" +
                escapeHtml(order.getShippingCity()) + ", " + escapeHtml(order.getShippingState()) + " - " + escapeHtml(order.getShippingPostalCode()) + "<br>" +
                "Phone: " + escapeHtml(order.getShippingPhone()) +
                "                  </div>" +
                "                </div>" +
                "              </td>" +
                "              <td width='50%' valign='top' style='padding-left:10px;'>" +
                "                <div style='background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:12px; font-size:12px; color:#475569; line-height:1.8;'>" +
                "                  <div style='display:flex; justify-content:space-between;'><span>Subtotal:</span> <span>₹" + (order.getSubtotal() != null ? order.getSubtotal().setScale(2, java.math.RoundingMode.HALF_UP) : "0.00") + "</span></div>" +
                (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0 ? "<div style='display:flex; justify-content:space-between; color:#10b981; font-weight:700;'><span>Discount:</span> <span>-₹" + order.getDiscountAmount().setScale(2, java.math.RoundingMode.HALF_UP) + "</span></div>" : "") +
                "                  <div style='display:flex; justify-content:space-between;'><span>Shipping:</span> <span style='color:#10b981; font-weight:700;'>" + (order.getShippingAmount() != null && order.getShippingAmount().compareTo(BigDecimal.ZERO) == 0 ? "FREE" : "₹" + order.getShippingAmount()) + "</span></div>" +
                "                  <div style='display:flex; justify-content:space-between; border-top:1px solid #e2e8f0; padding-top:6px; margin-top:4px; font-weight:900; color:#0f172a; font-size:14px;'>" +
                "                    <span>Total Paid:</span> <span style='color:#2563eb;'>₹" + (order.getTotalAmount() != null ? order.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP) : "0.00") + "</span>" +
                "                  </div>" +
                "                  <div style='font-size:11px; color:#64748b; margin-top:2px;'>Payment: <strong>" + escapeHtml(order.getPaymentMethod()) + " (" + escapeHtml(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "") + ")</strong></div>" +
                "                </div>" +
                "              </td>" +
                "            </tr>" +
                "          </table>" +
                // CTA Button
                "          <div style='text-align:center; margin-top:28px;'>" +
                "            <a href='" + appBaseUrl + "/order?id=" + order.getOrderId() + "' " +
                "               style='background:#f59e0b; color:#0f172a; text-decoration:none; font-weight:800; " +
                "                      font-size:14px; padding:12px 30px; border-radius:8px; display:inline-block;'>" +
                "              Track Your Order Live →" +
                "            </a>" +
                "          </div>" +
                "        </td></tr>" +
                // Footer
                "        <tr><td style='background:#f8fafc; border-top:1px solid #e5e7eb; padding:18px 32px; text-align:center;'>" +
                "          <p style='color:#94a3b8; font-size:12px; margin:0; line-height:1.5;'>" +
                "            &copy; 2026 ShopKart India Inc. &bull; 100% Purchase Protection &bull; Safe & Secure Delivery<br>" +
                "            Need assistance? Visit our <a href='" + appBaseUrl + "/orders' style='color:#2563eb; text-decoration:none;'>Customer Help Center</a>" +
                "          </p>" +
                "        </td></tr>" +
                "      </table>" +
                "    </td></tr>" +
                "  </table>" +
                "</body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
