package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.InitiatePaymentResponse;
import com.example.ecommerce.api.dto.PaymentFailureRequest;
import com.example.ecommerce.api.dto.VerifyPaymentRequest;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.PaymentStatus;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.payment.service.RazorpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Production-Grade Payment Gateway REST API integrating Razorpay.
 * Follows strict zero-trust principles:
 * 1. Razorpay secret is never exposed to the client.
 * 2. Amounts are calculated and verified exclusively from database state.
 * 3. Cryptographic HMAC-SHA256 payment signature verification is enforced server-side.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRestController.class);

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    @Autowired
    public PaymentRestController(OrderService orderService, PaymentService paymentService, RazorpayService razorpayService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    private UserSession getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
    }

    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @PathVariable int orderId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(orderId, user.getUserId());
        if (order == null) {
            throw new ResourceNotFoundException("Order not found or access denied: " + orderId);
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Order #" + order.getOrderNumber() + " has already been paid.");
        }

        String payMethod = (order.getPaymentMethod() != null && !"COD".equalsIgnoreCase(order.getPaymentMethod()))
                ? order.getPaymentMethod() : "ONLINE";
        paymentService.initiatePayment(order, payMethod);

        InitiatePaymentResponse resp = new InitiatePaymentResponse();
        resp.setOrderId(order.getOrderId());
        resp.setOrderNumber(order.getOrderNumber());
        resp.setAmount(order.getTotalAmount());
        resp.setAmountInPaise(order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue());
        resp.setCurrency("INR");
        resp.setCustomerName(order.getShippingFullName());
        resp.setCustomerEmail(user.getEmail());
        resp.setCustomerPhone(order.getShippingPhone());
        resp.setRazorpayConfigured(razorpayService.isConfigured());

        if (razorpayService.isConfigured()) {
            String razorpayOrderId = razorpayService.createRazorpayOrder(order);
            resp.setRazorpayOrderId(razorpayOrderId);
            resp.setRazorpayKeyId(razorpayService.getKeyId());
            logger.info("Razorpay order initiated: {} for Order #{}", razorpayOrderId, order.getOrderNumber());
        } else {
            logger.warn("Razorpay is unconfigured. Development simulation mode active for Order #{}", order.getOrderNumber());
        }

        return ResponseEntity.ok(ApiResponse.ok("Payment initiated", resp));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(req.getOrderId(), user.getUserId());
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + req.getOrderId());
        }

        if (razorpayService.isConfigured()) {
            // 1. Strict HMAC-SHA256 signature verification
            if (req.getRazorpayOrderId() == null || req.getRazorpayPaymentId() == null || req.getRazorpaySignature() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Missing cryptographic Razorpay payment verification parameters", "MISSING_SIGNATURE"));
            }

            boolean signatureValid = razorpayService.verifyPaymentSignature(
                    req.getRazorpayOrderId(),
                    req.getRazorpayPaymentId(),
                    req.getRazorpaySignature()
            );

            if (!signatureValid) {
                logger.error("SECURITY ALERT: Cryptographic signature mismatch for Order #{}", order.getOrderNumber());
                paymentService.processGatewayCallback(order.getOrderId(), user.getUserId(), req.getRazorpayPaymentId(), req.getRazorpayOrderId(), false, "Cryptographic signature validation failure");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Payment signature verification failed. Potential tampering detected.", "SIGNATURE_MISMATCH"));
            }

            // 2. Server-side payment fetch and capture verification
            boolean apiVerified = razorpayService.verifyAndFetchPayment(
                    req.getRazorpayPaymentId(),
                    req.getRazorpayOrderId(),
                    order.getTotalAmount()
            );

            if (!apiVerified) {
                logger.error("Razorpay server-side verification check failed for Order #{}", order.getOrderNumber());
                paymentService.processGatewayCallback(order.getOrderId(), user.getUserId(), req.getRazorpayPaymentId(), req.getRazorpayOrderId(), false, "Server-side Razorpay capture verification failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Payment could not be confirmed with gateway provider.", "PAYMENT_NOT_CAPTURED"));
            }

            // 3. Mark payment as successful in DB
            paymentService.processGatewayCallback(
                    order.getOrderId(),
                    user.getUserId(),
                    req.getRazorpayPaymentId(),
                    req.getRazorpayOrderId(),
                    true,
                    "Razorpay payment captured successfully: " + req.getRazorpayPaymentId()
            );

            logger.info("Payment verified successfully for Order #{} (Payment ID: {})", order.getOrderNumber(), req.getRazorpayPaymentId());

        } else {
            // Development simulation fallback: strictly disallowed in production or when ALLOW_PAYMENT_SIMULATION is not explicitly true
            boolean allowSimulation = "true".equalsIgnoreCase(System.getenv("ALLOW_PAYMENT_SIMULATION"))
                    || "true".equalsIgnoreCase(System.getProperty("allow.payment.simulation"));

            if (!allowSimulation) {
                logger.error("Payment verification rejected: Razorpay credentials not configured and simulation is disabled for Order #{}", order.getOrderNumber());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("Payment gateway is temporarily unavailable. Please choose Cash on Delivery or contact support.", "PAYMENT_GATEWAY_UNAVAILABLE"));
            }

            String txnRef = (req.getTransactionReference() != null && !req.getTransactionReference().trim().isEmpty())
                    ? req.getTransactionReference().trim()
                    : "DEV-SIM-" + System.currentTimeMillis();

            paymentService.processGatewayCallback(
                    order.getOrderId(),
                    user.getUserId(),
                    txnRef,
                    "DEV-SIM-GATEWAY-ORDER",
                    true,
                    "Non-production simulator confirmed payment"
            );
            logger.info("Non-production simulator confirmed payment for Order #{}", order.getOrderNumber());
        }

        return ResponseEntity.ok(ApiResponse.ok("Payment verified successfully", Map.of(
                "orderId", order.getOrderId(),
                "orderNumber", order.getOrderNumber(),
                "status", "PAID"
        )));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleRazorpayWebhook(
            @RequestBody(required = false) String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ) {
        if (payload == null || payload.trim().isEmpty() || signature == null || signature.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Missing webhook payload or signature", "INVALID_WEBHOOK_PAYLOAD"));
        }

        boolean verified = razorpayService.verifyWebhookSignature(payload, signature);
        if (!verified) {
            logger.error("Razorpay webhook signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Webhook signature mismatch", "UNAUTHORIZED"));
        }

        try {
            JSONObject eventObj = new JSONObject(payload);
            String event = eventObj.optString("event");
            logger.info("Received verified Razorpay webhook event: {}", event);

            if ("payment.captured".equalsIgnoreCase(event) || "order.paid".equalsIgnoreCase(event)) {
                JSONObject payloadObj = eventObj.getJSONObject("payload");
                JSONObject paymentEntity = payloadObj.getJSONObject("payment").getJSONObject("entity");
                String rzpPaymentId = paymentEntity.optString("id");
                String rzpOrderId = paymentEntity.optString("order_id");

                int internalOrderId = -1;
                if (paymentEntity.has("notes")) {
                    JSONObject notes = paymentEntity.getJSONObject("notes");
                    internalOrderId = notes.optInt("ecommerce_order_id", -1);
                }

                if (internalOrderId <= 0 && paymentEntity.has("receipt")) {
                    String receipt = paymentEntity.optString("receipt");
                    if (receipt.startsWith("rcpt_ord_")) {
                        try {
                            internalOrderId = Integer.parseInt(receipt.substring("rcpt_ord_".length()));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (internalOrderId > 0) {
                    paymentService.processServerWebhook(
                            internalOrderId,
                            rzpPaymentId,
                            rzpOrderId,
                            true,
                            "Webhook event: " + event + " (Payment ID: " + rzpPaymentId + ")"
                    );
                    logger.info("Order ID #{} successfully marked as PAID via webhook event {}", internalOrderId, event);
                }
            }

            return ResponseEntity.ok(ApiResponse.ok("Webhook processed successfully", Map.of("status", "ok")));
        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook payload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process webhook event", "WEBHOOK_PROCESSING_ERROR"));
        }
    }

    @PostMapping("/failure")
    public ResponseEntity<ApiResponse<Void>> recordPaymentFailure(
            @Valid @RequestBody PaymentFailureRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(req.getOrderId(), user.getUserId());
        if (order != null) {
            String reason = (req.getReason() != null && !req.getReason().trim().isEmpty())
                    ? req.getReason().trim() : "Payment failed or was cancelled by user";
            paymentService.processGatewayCallback(order.getOrderId(), user.getUserId(), "FAIL-" + System.currentTimeMillis(), "FAIL-ORDER", false, reason);
            logger.info("Payment failure recorded for Order #{}: {}", order.getOrderNumber(), reason);
        }

        return ResponseEntity.ok(ApiResponse.ok("Payment failure recorded", null));
    }
}
