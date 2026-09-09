package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.*;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.service.CouponService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.logistics.model.TrackingResult;
import com.example.ecommerce.logistics.service.LogisticsService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderReturnService;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.util.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for Customer Checkout, Order History, Cancellations, Returns, and Courier Live Tracking.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private static final Logger logger = LoggerFactory.getLogger(OrderRestController.class);

    private final OrderService orderService;
    private final CartService cartService;
    private final CouponService couponService;
    private final OrderReturnService orderReturnService;
    private final LogisticsService logisticsService;
    private final com.example.ecommerce.product.service.ProductService productService;

    @Autowired
    public OrderRestController(OrderService orderService, CartService cartService, CouponService couponService,
                               OrderReturnService orderReturnService, LogisticsService logisticsService,
                               com.example.ecommerce.product.service.ProductService productService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.couponService = couponService;
        this.orderReturnService = orderReturnService;
        this.logisticsService = logisticsService;
        this.productService = productService;
    }

    private UserSession getAuthenticatedCustomer(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        UserSession user = (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
        if (user != null && user.isAdmin()) {
            throw new ValidationException("Admin accounts are restricted from placing orders or accessing customer order APIs.");
        }
        return user;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(
            @Valid @RequestBody CheckoutRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to complete your checkout", "UNAUTHORIZED"));
        }

        HttpSession session = request.getSession(true);
        Coupon coupon = (Coupon) session.getAttribute("appliedCoupon");
        if (coupon == null && req.getCouponCode() != null && !req.getCouponCode().trim().isEmpty()) {
            Cart cart = cartService.getCart(user.getUserId());
            coupon = couponService.validateAndApplyCoupon(req.getCouponCode().trim().toUpperCase(), cart.getSubtotal(), user.getUserId());
        }

        String paymentMethod = (req.getPaymentMethod() != null && !req.getPaymentMethod().trim().isEmpty())
                ? req.getPaymentMethod().trim().toUpperCase() : "UPI";

        Order confirmed = orderService.processCheckout(
                user.getUserId(),
                req.getAddressId(),
                paymentMethod,
                req.getNotes(),
                coupon
        );

        // Clear session coupon and refresh session cart
        session.removeAttribute("appliedCoupon");
        session.setAttribute("cart", cartService.getCart(user.getUserId()));

        logger.info("Order placed via REST API: Order #{} by User ID: {}", confirmed.getOrderNumber(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", OrderDto.fromEntity(confirmed)));
    }

    @PostMapping("/direct-buy")
    public ResponseEntity<ApiResponse<OrderDto>> directBuy(
            @Valid @RequestBody DirectBuyRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to complete your purchase", "UNAUTHORIZED"));
        }

        Coupon coupon = null;
        if (req.getCouponCode() != null && !req.getCouponCode().trim().isEmpty()) {
            var prod = productService.getProductById(req.getProductId());
            BigDecimal itemSubtotal = prod.getDiscountedPrice().multiply(BigDecimal.valueOf(Math.max(1, req.getQuantity())));
            coupon = couponService.validateAndApplyCoupon(req.getCouponCode().trim().toUpperCase(), itemSubtotal, user.getUserId());
        }

        String paymentMethod = (req.getPaymentMethod() != null && !req.getPaymentMethod().trim().isEmpty())
                ? req.getPaymentMethod().trim().toUpperCase() : "UPI";

        Order confirmed = orderService.processDirectBuyCheckout(
                user.getUserId(),
                req.getAddressId(),
                req.getProductId(),
                req.getQuantity(),
                paymentMethod,
                req.getNotes(),
                coupon
        );

        logger.info("Direct Buy Order placed: Order #{} by User ID: {}", confirmed.getOrderNumber(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order placed successfully", OrderDto.fromEntity(confirmed)));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<CatalogPageResponse<OrderDto>>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to view your orders", "UNAUTHORIZED"));
        }

        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, Math.min(50, pageSize));
        Pagination<Order> rawPagination = orderService.getUserOrders(user.getUserId(), status, null, safePage, safePageSize);

        List<OrderDto> dtos = rawPagination.getItems().stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());

        Pagination<OrderDto> dtoPagination = new Pagination<>(
                dtos,
                rawPagination.getCurrentPage(),
                rawPagination.getPageSize(),
                rawPagination.getTotalItems()
        );

        return ResponseEntity.ok(ApiResponse.ok(CatalogPageResponse.from(dtoPagination)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable int orderId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(orderId, user.getUserId());
        if (order == null || order.getPaymentStatus() == com.example.ecommerce.order.model.PaymentStatus.FAILED) {
            throw new ResourceNotFoundException("Order not found or does not belong to your account: " + orderId);
        }

        return ResponseEntity.ok(ApiResponse.ok(OrderDto.fromEntity(order)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable int orderId,
            @RequestBody(required = false) CancelOrderRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        String reason = (req != null && req.getReason() != null) ? req.getReason().trim() : "Cancelled by customer";
        orderService.cancelOrder(orderId, user.getUserId(), reason);

        Order updated = orderService.getOrderById(orderId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", OrderDto.fromEntity(updated)));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<ApiResponse<Void>> requestReturn(
            @PathVariable int orderId,
            @Valid @RequestBody OrderReturnRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(orderId, user.getUserId());
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        orderReturnService.requestReturn(
                user.getUserId(),
                orderId,
                req.getReason(),
                "REFUND",
                req.getComments(),
                null
        );

        return ResponseEntity.ok(ApiResponse.ok("Return request submitted successfully", null));
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> getLiveTracking(
            @PathVariable int orderId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedCustomer(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Order order = orderService.getOrderById(orderId, user.getUserId());
        if (order == null) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderNumber(order.getOrderNumber());
        tracking.setStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : "PENDING");
        tracking.setCourierPartner(order.getCourierPartner());
        tracking.setTrackingNumber(order.getTrackingNumber());
        if (order.getEstimatedDeliveryDate() != null) {
            tracking.setEstimatedDelivery(order.getFormattedEstimatedDeliveryDate());
        }

        // First, add all internal status transition history records so customer sees admin updates immediately
        if (order.getStatusHistory() != null && !order.getStatusHistory().isEmpty()) {
            for (var sh : order.getStatusHistory()) {
                String timestampStr = sh.getCreatedAt() != null ? sh.getCreatedAt().toString().replace('T', ' ') : "";
                String statusName = sh.getNewStatus() != null ? sh.getNewStatus().name() : "UPDATE";
                String remarks = sh.getRemarks() != null && !sh.getRemarks().trim().isEmpty()
                        ? sh.getRemarks()
                        : (sh.getNewStatus() != null ? "Order transitioned to " + sh.getNewStatus().getDisplayName() : "Order updated");
                tracking.getEvents().add(new OrderTrackingResponse.TrackingEventDto(
                        timestampStr,
                        statusName,
                        "Fulfillment Center",
                        remarks
                ));
            }
        }

        if (order.getTrackingNumber() != null && !order.getTrackingNumber().trim().isEmpty() && order.getCourierPartner() != null) {
            try {
                TrackingResult tr = logisticsService.trackLiveShipment(order);
                if (tr != null) {
                    tracking.setTrackingUrl(tr.getCarrierTrackingUrl());
                    if (tr.getEstimatedDeliveryDate() != null) {
                        tracking.setEstimatedDelivery(tr.getFormattedEstimatedDeliveryDate());
                    }
                    if (tr.getScanHistory() != null && !tr.getScanHistory().isEmpty()) {
                        for (var ev : tr.getScanHistory()) {
                            tracking.getEvents().add(new OrderTrackingResponse.TrackingEventDto(
                                    ev.getFormattedTimestamp(),
                                    ev.getStatus() != null ? ev.getStatus().name() : ev.getRawStatus(),
                                    ev.getLocation(),
                                    ev.getActivity()
                            ));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // If no events exist yet, provide a baseline placed status event
        if (tracking.getEvents().isEmpty()) {
            String createdTime = order.getCreatedAt() != null ? order.getCreatedAt().toString().replace('T', ' ') : "Recently";
            tracking.getEvents().add(new OrderTrackingResponse.TrackingEventDto(
                    createdTime,
                    order.getOrderStatus() != null ? order.getOrderStatus().name() : "CONFIRMED",
                    "Online Order Gateway",
                    "Order placed and confirmed. Preparing for fulfillment."
            ));
        }

        return ResponseEntity.ok(ApiResponse.ok(tracking));
    }
}
