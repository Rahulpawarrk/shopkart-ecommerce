package com.example.ecommerce.util;

import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.PaymentStatus;
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

import org.springframework.stereotype.Service;

/**
 * Service for sending transactional emails (Password Resets, OTP Verification,
 * Order Invoices)
 * via Brevo/Resend HTTPS REST API (Port 443) or fallback SMTP.
 */
@Service
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

    /**
     * Sends a dedicated Return & Refund notification email to the customer.
     */
    public void sendOrderReturnEmail(String toEmail, String toName, OrderReturn orderReturn, String remarks) {
        if (orderReturn == null || toEmail == null || toEmail.trim().isEmpty()) {
            return;
        }

        mailExecutor.submit(() -> {
            try {
                String brevoApiKey = System.getenv("BREVO_API_KEY");
                String resendApiKey = System.getenv("RESEND_API_KEY");
                String subject = buildOrderReturnSubject(orderReturn.getReturnNumber(), orderReturn.getOrderNumber(),
                        orderReturn.getReturnStatus());
                String htmlBody = buildOrderReturnEmailHtml(toName, orderReturn, remarks);

                if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
                    sendViaBrevoHttps(brevoApiKey.trim(), toEmail, toName, subject, htmlBody);
                    return;
                }
                if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
                    sendViaResendHttps(resendApiKey.trim(), toEmail, subject, htmlBody);
                    return;
                }

                if (smtpEmail.isEmpty() || smtpPassword.isEmpty()) {
                    logger.info("Dev Mode: Order return [{}] update for Return #{}, Order #{}: Status {}",
                            orderReturn.getReturnNumber(), orderReturn.getReturnId(),
                            orderReturn.getOrderNumber(), orderReturn.getReturnStatus());
                    return;
                }

                Session session = buildMailSession();
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpEmail, fromName));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                message.setContent(htmlBody, "text/html; charset=UTF-8");

                Transport.send(message);
                logger.info("Order return [{}] update email sent to: {} for Return #{}",
                        orderReturn.getReturnStatus(), toEmail, orderReturn.getReturnNumber());

            } catch (Exception e) {
                logger.warn("Non-fatal: Email delivery failed for return #{}: {}",
                        orderReturn.getReturnNumber(), e.getMessage());
            }
        });
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private void sendViaBrevoHttps(String apiKey, String toEmail, String toName, String subject, String htmlContent) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            java.util.Map<String, Object> sender = new java.util.HashMap<>();
            sender.put("name", fromName);
            sender.put("email", (!smtpEmail.isEmpty() ? smtpEmail : "shopkart.support@gmail.com"));

            java.util.Map<String, Object> recipient = new java.util.HashMap<>();
            recipient.put("email", toEmail);
            if (toName != null && !toName.trim().isEmpty()) {
                recipient.put("name", toName.trim());
            }

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("sender", sender);
            payload.put("to", java.util.Collections.singletonList(recipient));
            payload.put("subject", subject != null ? subject : "ShopKart Notification");
            payload.put("htmlContent", htmlContent != null ? htmlContent : "");

            String jsonPayload = objectMapper.writeValueAsString(payload);

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
            String from = String.format("%s <%s>", fromName, (!smtpEmail.isEmpty() ? smtpEmail : "onboarding@resend.dev"));

            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("from", from);
            payload.put("to", java.util.Collections.singletonList(toEmail));
            payload.put("subject", subject != null ? subject : "ShopKart Notification");
            payload.put("html", htmlContent != null ? htmlContent : "");

            String jsonPayload = objectMapper.writeValueAsString(payload);

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

    public String buildOrderReturnSubject(String returnNumber, String orderNumber, String returnStatus) {
        String cleanStatus = returnStatus != null ? returnStatus.trim().toUpperCase() : "REQUESTED";
        String ord = (orderNumber != null && !orderNumber.isEmpty()) ? " (Order #" + orderNumber + ")" : "";
        switch (cleanStatus) {
            case "REQUESTED":
                return "↩ Return Request Received: #" + returnNumber + ord + " | ShopKart";
            case "APPROVED":
                return "✓ Return Request Approved: #" + returnNumber + ord + " | ShopKart";
            case "PICKUP_SCHEDULED":
                return "🚚 Return Pickup Scheduled: #" + returnNumber + ord + " | ShopKart";
            case "ITEM_RECEIVED":
                return "📦 Return Item Received at Warehouse: #" + returnNumber + ord + " | ShopKart";
            case "REFUNDED":
                return "💰 Refund Processed for Return #" + returnNumber + ord + " | ShopKart";
            case "REPLACED":
                return "📦 Replacement Dispatched for Return #" + returnNumber + ord + " | ShopKart";
            case "REJECTED":
                return "Notice on Return Request #" + returnNumber + ord + " | ShopKart";
            default:
                return "↩ Return Status Update: #" + returnNumber + ord + " | ShopKart";
        }
    }

    public String buildOrderStatusEmailHtml(String name, Order order, OrderStatus newStatus, String remarks) {
        if (order == null) return "";
        if (newStatus == OrderStatus.CANCELLED) {
            return buildOrderCancelledEmailHtml(name, order, remarks);
        } else if (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.PENDING) {
            return buildOrderConfirmedEmailHtml(name, order, remarks);
        } else {
            return buildGeneralStatusEmailHtml(name, order, newStatus, remarks);
        }
    }

    /**
     * Dedicated Email Template: Order Confirmed & Invoice Receipt
     */
    public String buildOrderConfirmedEmailHtml(String name, Order order, String remarks) {
        String custName = (name != null && !name.trim().isEmpty()) ? escapeHtml(name.trim()) : "Valued Customer";
        String orderNum = order.getOrderNumber() != null ? escapeHtml(order.getOrderNumber()) : "";
        String orderDate = (order.getFormattedCreatedAt() != null && !order.getFormattedCreatedAt().isEmpty())
                ? escapeHtml(order.getFormattedCreatedAt())
                : "Today";
        String payMethod = (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty())
                ? escapeHtml(order.getPaymentMethod())
                : "COD";
        String payStatus = (order.getPaymentStatus() != null)
                ? order.getPaymentStatus().name()
                : "PENDING";
        String estDelivery = (order.getFormattedEstimatedDeliveryDate() != null && !order.getFormattedEstimatedDeliveryDate().isEmpty())
                ? escapeHtml(order.getFormattedEstimatedDeliveryDate())
                : "Within 3 to 5 business days";

        StringBuilder itemsRows = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                itemsRows.append("<tr>")
                        .append("<td style='padding:12px 8px; font-size:13px; color:#1e293b; border-bottom:1px solid #f1f5f9;'>")
                        .append(escapeHtml(item.getProductName())).append("</td>")
                        .append("<td align='center' style='padding:12px 8px; font-size:13px; color:#64748b; border-bottom:1px solid #f1f5f9;'>")
                        .append(item.getQuantity()).append("</td>")
                        .append("<td align='right' style='padding:12px 8px; font-size:13px; font-weight:700; color:#0f172a; border-bottom:1px solid #f1f5f9;'>₹")
                        .append(item.getLineTotal() != null ? item.getLineTotal() : "0.00").append("</td>")
                        .append("</tr>");
            }
        } else {
            itemsRows.append("<tr><td colspan='3' style='padding:12px; font-size:13px; color:#64748b; text-align:center;'>Order Items</td></tr>");
        }

        StringBuilder discountRow = new StringBuilder();
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            discountRow.append("<tr>")
                    .append("<td align='right' style='padding:4px 0; font-size:13px; color:#16a34a;'>Discount:</td>")
                    .append("<td align='right' width='100' style='padding:4px 0; font-size:13px; color:#16a34a; font-weight:600;'>-₹")
                    .append(order.getDiscountAmount()).append("</td>")
                    .append("</tr>");
        }

        String shippingAddressHtml = buildShippingAddressHtml(order);

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Order Confirmation</title></head>"
                + "<body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif; background-color:#f1f5f9; margin:0; padding:24px 12px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px; background-color:#ffffff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 16px rgba(15,23,42,0.06);'>"
                
                // Brand Header
                + "<tr><td style='background-color:#0f172a; padding:24px 32px; text-align:center;'>"
                + "<span style='color:#f59e0b; font-size:26px; font-weight:800; letter-spacing:-0.5px; display:inline-block;'>🛒 ShopKart</span>"
                + "<span style='display:block; color:#94a3b8; font-size:11px; text-transform:uppercase; letter-spacing:1.5px; font-weight:600; margin-top:6px;'>Official Order Confirmation &amp; Invoice</span>"
                + "</td></tr>"

                // Hero Greeting
                + "<tr><td style='padding:32px 32px 20px 32px; text-align:left;'>"
                + "<div style='display:inline-block; background-color:#ecfdf5; border:1px solid #a7f3d0; border-radius:9999px; padding:6px 14px; margin-bottom:16px;'>"
                + "<span style='color:#059669; font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;'>✓ Order Confirmed</span></div>"
                + "<h1 style='color:#0f172a; font-size:22px; font-weight:800; margin:0 0 10px 0; line-height:1.3;'>Thank you for your order!</h1>"
                + "<p style='color:#475569; font-size:14px; line-height:1.6; margin:0 0 16px 0;'>"
                + "Hi <strong>" + custName + "</strong>, we have received your order <strong>#" + orderNum + "</strong>. Our warehouse team is now packing your items with care."
                + "</p></td></tr>"

                // Order Details Summary Card
                + "<tr><td style='padding:0 32px 20px 32px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Order Number</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>#" + orderNum + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Order Date</div><div style='font-size:14px; color:#0f172a; font-weight:600; margin-top:2px;'>" + orderDate + "</div></td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Payment Method</div><div style='font-size:14px; color:#0f172a; font-weight:600; margin-top:2px;'>" + payMethod + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Payment Status</div><div style='font-size:14px; color:#059669; font-weight:700; margin-top:2px;'>" + payStatus + "</div></td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' style='vertical-align:top; padding:8px 8px 4px 8px; border-top:1px dashed #e2e8f0; margin-top:6px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Estimated Delivery</div><div style='font-size:13px; color:#2563eb; font-weight:700; margin-top:2px;'>" + estDelivery + "</div></td>"
                + "</tr>"
                + "</table></td></tr>"

                // Shipping Address Card
                + "<tr><td style='padding:0 32px 20px 32px;'>" + shippingAddressHtml + "</td></tr>"

                // Items Table
                + "<tr><td style='padding:0 32px 16px 32px;'>"
                + "<div style='font-size:12px; color:#64748b; text-transform:uppercase; font-weight:700; letter-spacing:0.5px; margin-bottom:10px;'>Items in this Order</div>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='border-top:1px solid #e2e8f0; border-bottom:1px solid #e2e8f0;'>"
                + "<tr style='background-color:#f8fafc;'>"
                + "<th align='left' style='padding:10px 8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Item</th>"
                + "<th align='center' style='padding:10px 8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Qty</th>"
                + "<th align='right' style='padding:10px 8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Price</th>"
                + "</tr>"
                + itemsRows.toString()
                + "</table></td></tr>"

                // Price Breakdown
                + "<tr><td style='padding:0 32px 28px 32px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0'>"
                + "<tr><td align='right' style='padding:4px 0; font-size:13px; color:#64748b;'>Subtotal:</td><td align='right' width='110' style='padding:4px 0; font-size:13px; color:#0f172a; font-weight:600;'>₹" + (order.getSubtotal() != null ? order.getSubtotal() : "0.00") + "</td></tr>"
                + discountRow.toString()
                + "<tr><td align='right' style='padding:4px 0; font-size:13px; color:#64748b;'>Delivery Fee:</td><td align='right' width='110' style='padding:4px 0; font-size:13px; color:#059669; font-weight:600;'>FREE</td></tr>"
                + "<tr><td align='right' style='padding:10px 0 0 0; font-size:15px; font-weight:800; color:#0f172a; border-top:1px solid #e2e8f0;'>Total Amount:</td><td align='right' width='110' style='padding:10px 0 0 0; font-size:18px; font-weight:800; color:#2563eb; border-top:1px solid #e2e8f0;'>₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0.00") + "</td></tr>"
                + "</table></td></tr>"

                // CTA Track Order Button
                + "<tr><td align='center' style='padding:0 32px 32px 32px;'>"
                + "<a href='" + appBaseUrl + "/orders' target='_blank' style='background-color:#2563eb; color:#ffffff; text-decoration:none; font-weight:700; font-size:14px; padding:13px 32px; border-radius:8px; display:inline-block; box-shadow:0 2px 6px rgba(37,99,235,0.3);'>Track Your Order Online →</a>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background-color:#f8fafc; border-top:1px solid #e2e8f0; padding:24px 32px; text-align:center;'>"
                + "<p style='color:#64748b; font-size:12px; margin:0 0 6px 0;'>Need help with this order? Reply to this email or reach us at <a href='mailto:support@shopkart.com' style='color:#2563eb; text-decoration:none;'>support@shopkart.com</a>.</p>"
                + "<p style='color:#94a3b8; font-size:11px; margin:0;'>© 2026 ShopKart India. All rights reserved. • India's Premier Online Shopping Destination</p>"
                + "</td></tr></table></td></tr></table></body></html>";
    }

    /**
     * Dedicated Email Template: Order Cancelled & Refund Advisory
     */
    public String buildOrderCancelledEmailHtml(String name, Order order, String remarks) {
        String custName = (name != null && !name.trim().isEmpty()) ? escapeHtml(name.trim()) : "Valued Customer";
        String orderNum = order.getOrderNumber() != null ? escapeHtml(order.getOrderNumber()) : "";
        String cancelRemarks = (remarks != null && !remarks.trim().isEmpty()) ? escapeHtml(remarks.trim()) : "Cancelled by Customer";
        String totalAmountStr = order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0.00";

        boolean isPrepaid = (order.getPaymentMethod() != null && !"COD".equalsIgnoreCase(order.getPaymentMethod()))
                || order.getPaymentStatus() == PaymentStatus.PAID
                || order.getPaymentStatus() == PaymentStatus.REFUNDED;

        String refundAdvisoryHtml;
        if (isPrepaid) {
            refundAdvisoryHtml = "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#eff6ff; border:1px solid #bfdbfe; border-radius:10px; padding:16px;'>"
                    + "<tr><td>"
                    + "<div style='font-size:13px; font-weight:700; color:#1e40af; margin-bottom:6px;'>💳 Refund Initiated (Online Payment)</div>"
                    + "<p style='font-size:13px; color:#1e3a8a; line-height:1.5; margin:0;'>"
                    + "Since your order was paid online via <strong>" + escapeHtml(order.getPaymentMethod() != null ? order.getPaymentMethod() : "Prepaid") + "</strong>, a full refund of <strong>₹" + totalAmountStr + "</strong> has been initiated back to your original payment method. The amount will reflect in your account within <strong>5 to 7 business days</strong> depending on your bank."
                    + "</p></td></tr></table>";
        } else {
            refundAdvisoryHtml = "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                    + "<tr><td>"
                    + "<div style='font-size:13px; font-weight:700; color:#334155; margin-bottom:6px;'>💵 Cash on Delivery (COD)</div>"
                    + "<p style='font-size:13px; color:#475569; line-height:1.5; margin:0;'>"
                    + "Since this order was scheduled for Cash on Delivery, no payment was collected and no further refund action is required."
                    + "</p></td></tr></table>";
        }

        StringBuilder itemsRows = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                itemsRows.append("<tr>")
                        .append("<td style='padding:10px 8px; font-size:13px; color:#1e293b; border-bottom:1px solid #f1f5f9;'>")
                        .append(escapeHtml(item.getProductName())).append("</td>")
                        .append("<td align='center' style='padding:10px 8px; font-size:13px; color:#64748b; border-bottom:1px solid #f1f5f9;'>")
                        .append(item.getQuantity()).append("</td>")
                        .append("<td align='right' style='padding:10px 8px; font-size:13px; font-weight:700; color:#0f172a; border-bottom:1px solid #f1f5f9;'>₹")
                        .append(item.getLineTotal() != null ? item.getLineTotal() : "0.00").append("</td>")
                        .append("</tr>");
            }
        } else {
            itemsRows.append("<tr><td colspan='3' style='padding:12px; font-size:13px; color:#64748b; text-align:center;'>Order Items</td></tr>");
        }

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Order Cancelled</title></head>"
                + "<body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif; background-color:#f1f5f9; margin:0; padding:24px 12px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px; background-color:#ffffff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 16px rgba(15,23,42,0.06);'>"
                
                // Brand Header
                + "<tr><td style='background-color:#0f172a; padding:24px 32px; text-align:center;'>"
                + "<span style='color:#f59e0b; font-size:26px; font-weight:800; letter-spacing:-0.5px; display:inline-block;'>🛒 ShopKart</span>"
                + "<span style='display:block; color:#94a3b8; font-size:11px; text-transform:uppercase; letter-spacing:1.5px; font-weight:600; margin-top:6px;'>Order Cancellation Notice</span>"
                + "</td></tr>"

                // Hero Greeting
                + "<tr><td style='padding:32px 32px 20px 32px; text-align:left;'>"
                + "<div style='display:inline-block; background-color:#fee2e2; border:1px solid #fecaca; border-radius:9999px; padding:6px 14px; margin-bottom:16px;'>"
                + "<span style='color:#b91c1c; font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;'>✕ Order Cancelled</span></div>"
                + "<h1 style='color:#0f172a; font-size:22px; font-weight:800; margin:0 0 10px 0; line-height:1.3;'>Your order has been cancelled</h1>"
                + "<p style='color:#475569; font-size:14px; line-height:1.6; margin:0 0 16px 0;'>"
                + "Hi <strong>" + custName + "</strong>, this email confirms that your order <strong>#" + orderNum + "</strong> has been successfully cancelled."
                + "</p></td></tr>"

                // Cancellation Details Card
                + "<tr><td style='padding:0 32px 20px 32px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Order Number</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>#" + orderNum + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Cancelled Order Total</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>₹" + totalAmountStr + "</div></td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' style='vertical-align:top; padding:8px 8px 4px 8px; border-top:1px dashed #e2e8f0; margin-top:6px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600; letter-spacing:0.5px;'>Cancellation Reason / Remarks</div><div style='font-size:13px; color:#334155; font-weight:500; margin-top:2px;'>" + cancelRemarks + "</div></td>"
                + "</tr>"
                + "</table></td></tr>"

                // Refund Advisory Box
                + "<tr><td style='padding:0 32px 20px 32px;'>" + refundAdvisoryHtml + "</td></tr>"

                // Cancelled Items
                + "<tr><td style='padding:0 32px 24px 32px;'>"
                + "<div style='font-size:12px; color:#64748b; text-transform:uppercase; font-weight:700; letter-spacing:0.5px; margin-bottom:10px;'>Cancelled Items</div>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='border-top:1px solid #e2e8f0; border-bottom:1px solid #e2e8f0;'>"
                + "<tr style='background-color:#f8fafc;'>"
                + "<th align='left' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Item</th>"
                + "<th align='center' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Qty</th>"
                + "<th align='right' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Price</th>"
                + "</tr>"
                + itemsRows.toString()
                + "</table></td></tr>"

                // CTA Button
                + "<tr><td align='center' style='padding:0 32px 32px 32px;'>"
                + "<a href='" + appBaseUrl + "/products' target='_blank' style='background-color:#0f172a; color:#f8fafc; text-decoration:none; font-weight:700; font-size:14px; padding:13px 32px; border-radius:8px; display:inline-block; box-shadow:0 2px 6px rgba(15,23,42,0.2);'>Browse ShopKart Catalog →</a>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background-color:#f8fafc; border-top:1px solid #e2e8f0; padding:24px 32px; text-align:center;'>"
                + "<p style='color:#64748b; font-size:12px; margin:0 0 6px 0;'>Did not request this cancellation? Contact our team immediately at <a href='mailto:support@shopkart.com' style='color:#2563eb; text-decoration:none;'>support@shopkart.com</a>.</p>"
                + "<p style='color:#94a3b8; font-size:11px; margin:0;'>© 2026 ShopKart India. All rights reserved.</p>"
                + "</td></tr></table></td></tr></table></body></html>";
    }

    /**
     * Dedicated Email Template: Order Return & Replacement Status Updates
     */
    public String buildOrderReturnEmailHtml(String name, OrderReturn ret, String remarks) {
        if (ret == null) return "";
        String custName = (name != null && !name.trim().isEmpty()) ? escapeHtml(name.trim()) : "Valued Customer";
        String returnNum = ret.getReturnNumber() != null ? escapeHtml(ret.getReturnNumber()) : "";
        String orderNum = ret.getOrderNumber() != null ? escapeHtml(ret.getOrderNumber()) : "";
        String resolution = (ret.getResolutionType() != null && !ret.getResolutionType().isEmpty()) ? escapeHtml(ret.getResolutionType()) : "REFUND";
        String reason = (ret.getReturnReason() != null && !ret.getReturnReason().isEmpty()) ? escapeHtml(ret.getReturnReason()) : "Return Request";
        String status = (ret.getReturnStatus() != null && !ret.getReturnStatus().isEmpty()) ? ret.getReturnStatus().trim().toUpperCase() : "REQUESTED";

        String badgeBg = "#f5f3ff";
        String badgeBorder = "#ddd6fe";
        String badgeColor = "#6d28d9";
        String badgeText = "↩ " + status;
        String headline = "Return Request Status Update";
        String introMessage;

        switch (status) {
            case "REQUESTED":
                badgeBg = "#f5f3ff"; badgeBorder = "#ddd6fe"; badgeColor = "#6d28d9";
                badgeText = "↩ Return Request Received";
                headline = "We Have Received Your Return Request";
                introMessage = "Hi <strong>" + custName + "</strong>, we have received your return request for Order <strong>#" + orderNum + "</strong> (Return Ref: <strong>#" + returnNum + "</strong>). Our team is reviewing the details and will schedule pickup shortly.";
                break;
            case "APPROVED":
                badgeBg = "#ecfdf5"; badgeBorder = "#bbf7d0"; badgeColor = "#15803d";
                badgeText = "✓ Return Request Approved";
                headline = "Your Return Request is Approved!";
                introMessage = "Hi <strong>" + custName + "</strong>, good news! Your return request for Order <strong>#" + orderNum + "</strong> has been approved. Our logistics partner will be assigned for pickup.";
                break;
            case "PICKUP_SCHEDULED":
                badgeBg = "#eff6ff"; badgeBorder = "#bfdbfe"; badgeColor = "#1d4ed8";
                badgeText = "🚚 Pickup Scheduled";
                headline = "Return Pickup Scheduled";
                introMessage = "Hi <strong>" + custName + "</strong>, a courier pickup has been scheduled for your return package. Please keep the item safely packed with all tags intact.";
                break;
            case "ITEM_RECEIVED":
                badgeBg = "#fffbeb"; badgeBorder = "#fde68a"; badgeColor = "#b45309";
                badgeText = "📦 Item Received at Warehouse";
                headline = "Return Item Received";
                introMessage = "Hi <strong>" + custName + "</strong>, your return package has arrived at our fulfillment center and is currently undergoing quality inspection.";
                break;
            case "REFUNDED":
                badgeBg = "#ecfdf5"; badgeBorder = "#a7f3d0"; badgeColor = "#047857";
                badgeText = "💰 Refund Processed Successfully";
                headline = "Your Refund Has Been Processed!";
                introMessage = "Hi <strong>" + custName + "</strong>, your return item has passed inspection. A refund of <strong>₹" + (ret.getRefundAmount() != null ? ret.getRefundAmount() : "0.00") + "</strong> has been credited back to your original payment method.";
                break;
            case "REPLACED":
                badgeBg = "#f0fdfa"; badgeBorder = "#99f6e4"; badgeColor = "#0f766e";
                badgeText = "📦 Replacement Processed";
                headline = "Your Replacement Item is on its Way!";
                introMessage = "Hi <strong>" + custName + "</strong>, your replacement item has been processed and is being dispatched.";
                break;
            case "REJECTED":
                badgeBg = "#fef2f2"; badgeBorder = "#fecaca"; badgeColor = "#b91c1c";
                badgeText = "✕ Return Request Declined";
                headline = "Update Regarding Return Request";
                introMessage = "Hi <strong>" + custName + "</strong>, following inspection and review against our return policy, your return request could not be approved at this time.";
                break;
            default:
                introMessage = "Hi <strong>" + custName + "</strong>, your return request status has been updated to <strong>" + status + "</strong>.";
                break;
        }

        StringBuilder optionalRows = new StringBuilder();
        if (ret.getRefundAmount() != null && ret.getRefundAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            optionalRows.append("<tr>")
                    .append("<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Refund Amount</div><div style='font-size:14px; color:#047857; font-weight:700; margin-top:2px;'>₹")
                    .append(ret.getRefundAmount()).append("</div></td>")
                    .append("<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Resolution</div><div style='font-size:14px; color:#0f172a; font-weight:600; margin-top:2px;'>")
                    .append(resolution).append("</div></td>")
                    .append("</tr>");
        }
        if (ret.getComments() != null && !ret.getComments().trim().isEmpty()) {
            optionalRows.append("<tr><td colspan='2' style='vertical-align:top; padding:8px 8px 4px 8px; border-top:1px dashed #e2e8f0; margin-top:4px;'>")
                    .append("<div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Customer Comments</div><div style='font-size:13px; color:#334155; margin-top:2px;'>")
                    .append(escapeHtml(ret.getComments().trim())).append("</div></td></tr>");
        }
        String adminNoteText = (ret.getAdminNotes() != null && !ret.getAdminNotes().trim().isEmpty())
                ? ret.getAdminNotes().trim()
                : (remarks != null && !remarks.trim().isEmpty() ? remarks.trim() : null);
        if (adminNoteText != null) {
            optionalRows.append("<tr><td colspan='2' style='vertical-align:top; padding:8px 8px 4px 8px; border-top:1px dashed #e2e8f0; margin-top:4px;'>")
                    .append("<div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Support Team Remarks</div><div style='font-size:13px; color:#334155; margin-top:2px;'>")
                    .append(escapeHtml(adminNoteText)).append("</div></td></tr>");
        }

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Return Status Update</title></head>"
                + "<body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif; background-color:#f1f5f9; margin:0; padding:24px 12px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px; background-color:#ffffff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 16px rgba(15,23,42,0.06);'>"
                
                // Brand Header
                + "<tr><td style='background-color:#0f172a; padding:24px 32px; text-align:center;'>"
                + "<span style='color:#f59e0b; font-size:26px; font-weight:800; letter-spacing:-0.5px; display:inline-block;'>🛒 ShopKart</span>"
                + "<span style='display:block; color:#94a3b8; font-size:11px; text-transform:uppercase; letter-spacing:1.5px; font-weight:600; margin-top:6px;'>Return &amp; Refund Services</span>"
                + "</td></tr>"

                // Hero Greeting
                + "<tr><td style='padding:32px 32px 20px 32px; text-align:left;'>"
                + "<div style='display:inline-block; background-color:" + badgeBg + "; border:1px solid " + badgeBorder + "; border-radius:9999px; padding:6px 14px; margin-bottom:16px;'>"
                + "<span style='color:" + badgeColor + "; font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;'>" + badgeText + "</span></div>"
                + "<h1 style='color:#0f172a; font-size:22px; font-weight:800; margin:0 0 10px 0; line-height:1.3;'>" + headline + "</h1>"
                + "<p style='color:#475569; font-size:14px; line-height:1.6; margin:0 0 16px 0;'>" + introMessage + "</p></td></tr>"

                // Return Details Card
                + "<tr><td style='padding:0 32px 20px 32px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Return Reference</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>#" + returnNum + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Original Order</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>#" + orderNum + "</div></td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Return Reason</div><div style='font-size:13px; color:#334155; font-weight:500; margin-top:2px;'>" + reason + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Resolution Type</div><div style='font-size:13px; color:#334155; font-weight:600; margin-top:2px;'>" + resolution + "</div></td>"
                + "</tr>"
                + optionalRows.toString()
                + "</table></td></tr>"

                // Return Guidelines
                + "<tr><td style='padding:0 32px 24px 32px;'>"
                + "<div style='background-color:#f1f5f9; border-radius:10px; padding:16px;'>"
                + "<div style='font-size:12px; color:#475569; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; margin-bottom:8px;'>📦 Helpful Return Instructions:</div>"
                + "<ul style='margin:0; padding-left:18px; color:#64748b; font-size:13px; line-height:1.6;'>"
                + "<li>Please keep the product in its original packaging with tags, invoices, and brand boxes intact.</li>"
                + "<li>Hand over the sealed box to our logistics partner when they arrive for pickup.</li>"
                + "<li>Refunds are credited to your original payment method within 5–7 business days after warehouse quality check.</li>"
                + "</ul></div></td></tr>"

                // CTA Button
                + "<tr><td align='center' style='padding:0 32px 32px 32px;'>"
                + "<a href='" + appBaseUrl + "/orders' target='_blank' style='background-color:#6d28d9; color:#ffffff; text-decoration:none; font-weight:700; font-size:14px; padding:13px 32px; border-radius:8px; display:inline-block; box-shadow:0 2px 6px rgba(109,40,217,0.3);'>View Return in My Orders →</a>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background-color:#f8fafc; border-top:1px solid #e2e8f0; padding:24px 32px; text-align:center;'>"
                + "<p style='color:#64748b; font-size:12px; margin:0 0 6px 0;'>Have queries about returns? Contact us at <a href='mailto:support@shopkart.com' style='color:#6d28d9; text-decoration:none;'>support@shopkart.com</a>.</p>"
                + "<p style='color:#94a3b8; font-size:11px; margin:0;'>© 2026 ShopKart India. All rights reserved.</p>"
                + "</td></tr></table></td></tr></table></body></html>";
    }

    /**
     * General Order Status Updates (Processing, Dispatched, Delivered, etc.)
     */
    public String buildGeneralStatusEmailHtml(String name, Order order, OrderStatus status, String remarks) {
        String custName = (name != null && !name.trim().isEmpty()) ? escapeHtml(name.trim()) : "Valued Customer";
        String orderNum = order.getOrderNumber() != null ? escapeHtml(order.getOrderNumber()) : "";
        String statusName = status != null ? status.getDisplayName() : "In Progress";
        
        String heroTitle;
        String heroSub;
        if (status == OrderStatus.DELIVERED) {
            heroTitle = "Your Order Has Arrived! 🎉";
            heroSub = "Hi <strong>" + custName + "</strong>, your order <strong>#" + orderNum + "</strong> has been successfully delivered. We hope you love your purchase!";
        } else if (status == OrderStatus.DISPATCHED) {
            heroTitle = "Your Order is on the Way! 🚚";
            heroSub = "Hi <strong>" + custName + "</strong>, your order <strong>#" + orderNum + "</strong> has been dispatched" +
                    (order.getCourierPartner() != null ? " with <strong>" + escapeHtml(order.getCourierPartner()) + "</strong>" : "") + ".";
        } else {
            heroTitle = "Order Status Update: " + statusName;
            heroSub = "Hi <strong>" + custName + "</strong>, your order <strong>#" + orderNum + "</strong> is now <strong>" + statusName + "</strong>.";
        }

        StringBuilder itemsRows = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                itemsRows.append("<tr>")
                        .append("<td style='padding:10px 8px; font-size:13px; color:#1e293b; border-bottom:1px solid #f1f5f9;'>")
                        .append(escapeHtml(item.getProductName())).append("</td>")
                        .append("<td align='center' style='padding:10px 8px; font-size:13px; color:#64748b; border-bottom:1px solid #f1f5f9;'>")
                        .append(item.getQuantity()).append("</td>")
                        .append("<td align='right' style='padding:10px 8px; font-size:13px; font-weight:700; color:#0f172a; border-bottom:1px solid #f1f5f9;'>₹")
                        .append(item.getLineTotal() != null ? item.getLineTotal() : "0.00").append("</td>")
                        .append("</tr>");
            }
        }

        StringBuilder logisticsRows = new StringBuilder();
        if (order.getCourierPartner() != null && !order.getCourierPartner().isEmpty()) {
            logisticsRows.append("<tr><td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Courier Partner</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>")
                    .append(escapeHtml(order.getCourierPartner())).append("</div></td>");
            if (order.getTrackingNumber() != null && !order.getTrackingNumber().isEmpty()) {
                logisticsRows.append("<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Tracking Number</div><div style='font-size:14px; color:#2563eb; font-weight:700; margin-top:2px;'>")
                        .append(escapeHtml(order.getTrackingNumber())).append("</div></td>");
            } else {
                logisticsRows.append("<td width='50%'></td>");
            }
            logisticsRows.append("</tr>");
        }

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><title>Order Status Update</title></head>"
                + "<body style='font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif; background-color:#f1f5f9; margin:0; padding:24px 12px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='max-width:600px; background-color:#ffffff; border-radius:12px; border:1px solid #e2e8f0; overflow:hidden; box-shadow:0 4px 16px rgba(15,23,42,0.06);'>"
                
                // Brand Header
                + "<tr><td style='background-color:#0f172a; padding:24px 32px; text-align:center;'>"
                + "<span style='color:#f59e0b; font-size:26px; font-weight:800; letter-spacing:-0.5px; display:inline-block;'>🛒 ShopKart</span>"
                + "<span style='display:block; color:#94a3b8; font-size:11px; text-transform:uppercase; letter-spacing:1.5px; font-weight:600; margin-top:6px;'>Order Status Update</span>"
                + "</td></tr>"

                // Hero Greeting
                + "<tr><td style='padding:32px 32px 20px 32px; text-align:left;'>"
                + "<div style='display:inline-block; background-color:#eff6ff; border:1px solid #bfdbfe; border-radius:9999px; padding:6px 14px; margin-bottom:16px;'>"
                + "<span style='color:#1d4ed8; font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:0.5px;'>Status: " + statusName + "</span></div>"
                + "<h1 style='color:#0f172a; font-size:22px; font-weight:800; margin:0 0 10px 0; line-height:1.3;'>" + heroTitle + "</h1>"
                + "<p style='color:#475569; font-size:14px; line-height:1.6; margin:0 0 16px 0;'>" + heroSub + "</p></td></tr>"

                // Logistics / Status Overview Card
                + "<tr><td style='padding:0 32px 20px 32px;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                + "<tr>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Order Number</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>#" + orderNum + "</div></td>"
                + "<td width='50%' style='vertical-align:top; padding:6px 8px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Total Amount</div><div style='font-size:14px; color:#0f172a; font-weight:700; margin-top:2px;'>₹" + (order.getTotalAmount() != null ? order.getTotalAmount() : "0.00") + "</div></td>"
                + "</tr>"
                + logisticsRows.toString()
                + (remarks != null && !remarks.trim().isEmpty() ? "<tr><td colspan='2' style='vertical-align:top; padding:8px 8px 4px 8px; border-top:1px dashed #e2e8f0; margin-top:4px;'><div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:600;'>Remarks</div><div style='font-size:13px; color:#334155; margin-top:2px;'>" + escapeHtml(remarks.trim()) + "</div></td></tr>" : "")
                + "</table></td></tr>"

                // Items summary
                + (itemsRows.length() > 0 ? "<tr><td style='padding:0 32px 24px 32px;'>"
                + "<div style='font-size:12px; color:#64748b; text-transform:uppercase; font-weight:700; letter-spacing:0.5px; margin-bottom:10px;'>Order Items</div>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='border-top:1px solid #e2e8f0; border-bottom:1px solid #e2e8f0;'>"
                + "<tr style='background-color:#f8fafc;'>"
                + "<th align='left' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Item</th>"
                + "<th align='center' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Qty</th>"
                + "<th align='right' style='padding:8px; font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700;'>Price</th>"
                + "</tr>"
                + itemsRows.toString()
                + "</table></td></tr>" : "")

                // CTA Button
                + "<tr><td align='center' style='padding:0 32px 32px 32px;'>"
                + "<a href='" + appBaseUrl + "/orders' target='_blank' style='background-color:#2563eb; color:#ffffff; text-decoration:none; font-weight:700; font-size:14px; padding:13px 32px; border-radius:8px; display:inline-block; box-shadow:0 2px 6px rgba(37,99,235,0.3);'>View Order Online →</a>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='background-color:#f8fafc; border-top:1px solid #e2e8f0; padding:24px 32px; text-align:center;'>"
                + "<p style='color:#64748b; font-size:12px; margin:0 0 6px 0;'>Need help? Email us at <a href='mailto:support@shopkart.com' style='color:#2563eb; text-decoration:none;'>support@shopkart.com</a>.</p>"
                + "<p style='color:#94a3b8; font-size:11px; margin:0;'>© 2026 ShopKart India. All rights reserved.</p>"
                + "</td></tr></table></td></tr></table></body></html>";
    }

    private String buildShippingAddressHtml(Order order) {
        String fullName = order.getShippingFullName() != null ? escapeHtml(order.getShippingFullName()) : "";
        StringBuilder lines = new StringBuilder();
        if (order.getShippingAddressLine1() != null && !order.getShippingAddressLine1().isEmpty()) {
            lines.append(escapeHtml(order.getShippingAddressLine1()));
        }
        if (order.getShippingAddressLine2() != null && !order.getShippingAddressLine2().isEmpty()) {
            if (lines.length() > 0) lines.append(", ");
            lines.append(escapeHtml(order.getShippingAddressLine2()));
        }
        StringBuilder cityStatePin = new StringBuilder();
        if (order.getShippingCity() != null && !order.getShippingCity().isEmpty()) {
            cityStatePin.append(escapeHtml(order.getShippingCity()));
        }
        if (order.getShippingState() != null && !order.getShippingState().isEmpty()) {
            if (cityStatePin.length() > 0) cityStatePin.append(", ");
            cityStatePin.append(escapeHtml(order.getShippingState()));
        }
        if (order.getShippingPostalCode() != null && !order.getShippingPostalCode().isEmpty()) {
            if (cityStatePin.length() > 0) cityStatePin.append(" - ");
            cityStatePin.append(escapeHtml(order.getShippingPostalCode()));
        }
        String phone = order.getShippingPhone() != null ? escapeHtml(order.getShippingPhone()) : "";

        return "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' border='0' style='background-color:#f8fafc; border:1px solid #e2e8f0; border-radius:10px; padding:16px;'>"
                + "<tr><td>"
                + "<div style='font-size:11px; color:#64748b; text-transform:uppercase; font-weight:700; letter-spacing:0.5px; margin-bottom:6px;'>📍 Delivery Address</div>"
                + (fullName.isEmpty() ? "" : "<div style='font-size:14px; font-weight:700; color:#0f172a;'>" + fullName + "</div>")
                + (lines.length() == 0 ? "" : "<div style='font-size:13px; color:#475569; line-height:1.5; margin-top:2px;'>" + lines.toString() + "</div>")
                + (cityStatePin.length() == 0 ? "" : "<div style='font-size:13px; color:#475569; line-height:1.5; margin-top:2px;'>" + cityStatePin.toString() + "</div>")
                + (phone.isEmpty() ? "" : "<div style='font-size:12px; color:#64748b; margin-top:4px;'>Phone: " + phone + "</div>")
                + "</td></tr></table>";
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

    private String escapeHtml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}