package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.dao.CartDAO;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.coupon.dao.CouponDAO;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.customer.service.AddressService;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.model.TransactionType;
import com.example.ecommerce.inventory.service.InventoryService;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.OrderStatusHistory;
import com.example.ecommerce.order.model.PaymentStatus;
import com.example.ecommerce.util.EmailService;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer coordinating Atomic Order Processing, Checkout,
 * Inventory Reservation, Status Transitions, and Cancellations.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderDAO orderDAO;
    private final CartService cartService;
    private final CartDAO cartDAO;
    private final AddressService addressService;
    private final InventoryService inventoryService;
    private final CouponDAO couponDAO;
    private final EmailService emailService;
    private final ProductDAO productDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.cartService = new CartService();
        this.cartDAO = new CartDAO();
        this.addressService = new AddressService();
        this.inventoryService = new InventoryService();
        this.couponDAO = new CouponDAO();
        this.emailService = new EmailService();
        this.productDAO = new ProductDAO();
    }

    public OrderService(OrderDAO orderDAO, CartService cartService, CartDAO cartDAO, 
                        AddressService addressService, InventoryService inventoryService) {
        this(orderDAO, cartService, cartDAO, addressService, inventoryService, new EmailService(), new ProductDAO());
    }

    public OrderService(OrderDAO orderDAO, CartService cartService, CartDAO cartDAO, 
                        AddressService addressService, InventoryService inventoryService, EmailService emailService) {
        this(orderDAO, cartService, cartDAO, addressService, inventoryService, emailService, new ProductDAO());
    }

    @Autowired
    public OrderService(OrderDAO orderDAO, CartService cartService, CartDAO cartDAO, 
                        AddressService addressService, InventoryService inventoryService, 
                        EmailService emailService, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.cartService = cartService;
        this.cartDAO = cartDAO;
        this.addressService = addressService;
        this.inventoryService = inventoryService;
        this.couponDAO = new CouponDAO();
        this.emailService = emailService != null ? emailService : new EmailService();
        this.productDAO = productDAO != null ? productDAO : new ProductDAO();
    }

    /**
     * Executes Atomic Multi-Step Checkout Transaction with optional coupon.
     */
    public Order processCheckout(int userId, int addressId, String paymentMethod, String notes, Coupon appliedCoupon) {
        // 1. Verify Cart
        Cart cart = cartService.getCart(userId);
        if (cart.isEmpty()) {
            throw new ValidationException("Cannot checkout: Shopping cart is empty.");
        }
        if (cart.hasUnavailableItems()) {
            throw new ValidationException("Some items in your cart are out of stock. Please update your cart.");
        }
        for (CartItem item : cart.getItems()) {
            if (item.getQuantity() > 3) {
                throw new ValidationException("Purchase limit exceeded: Product '" + item.getProductName() + 
                        "' exceeds the maximum limit of 3 units per customer.");
            }
        }

        if (appliedCoupon != null) {
            BigDecimal couponDiscount = appliedCoupon.calculateDiscount(cart.getSubtotal());
            cart.setCouponId(appliedCoupon.getCouponId());
            cart.setAppliedCouponCode(appliedCoupon.getCode());
            cart.setCouponDiscount(couponDiscount);
        }

        // 2. Verify Shipping Address
        Address address = addressService.getAddressById(addressId, userId);

        // 3. Prepare Order Entity
        String orderNumber = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + 
                             UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setCouponId(cart.getCouponId());
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        order.setNotes(notes);

        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        // Financials from Authoritative Cart
        order.setSubtotal(cart.getSubtotal());
        order.setDiscountAmount(cart.getTotalSavings());
        order.setTaxAmount(cart.getEstimatedTax());
        order.setShippingAmount(BigDecimal.ZERO); // Free shipping
        order.setTotalAmount(cart.getGrandTotal());

        // Snapshot Address Details
        order.setShippingFullName(address.getFullName());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountry(address.getCountry());
        order.setBillingAddressSnapshot(address.getFormattedAddress());

        // 4. ATOMIC DATABASE TRANSACTION
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                // Step A: Insert Master Order
                int orderId = orderDAO.createOrder(order, conn);

                // Step B: Increment Coupon Usage if applicable
                if (order.getCouponId() != null) {
                    boolean success = couponDAO.incrementUsedCountAtomic(order.getCouponId(), conn);
                    if (!success) {
                        throw new ValidationException("Coupon usage limit has been reached.");
                    }
                    couponDAO.recordCouponUsage(order.getCouponId(), userId, orderId, cart.getCouponDiscount(), conn);
                }

                // Step C: Insert Order Line Items & Reserve Stock with UPDLOCK
                for (CartItem cartItem : cart.getItems()) {
                    OrderItem orderItem = new OrderItem(
                            orderId,
                            cartItem.getProductId(),
                            cartItem.getProductName(),
                            cartItem.getSku(),
                            cartItem.getUnitPrice(),
                            cartItem.getDiscountAmount(),
                            cartItem.getLineTax(),
                            cartItem.getLineTotal(),
                            cartItem.getQuantity()
                    );
                    orderDAO.createOrderItem(orderItem, conn);

                    // Deduct Stock atomically with row-level lock
                    inventoryService.reserveStockForOrder(cartItem.getProductId(), cartItem.getQuantity(), orderId, conn);
                }

                // Step D: Clear Shopping Cart
                cartDAO.clearCart(cart.getCartId(), conn);

                // Step E: Insert Initial Lifecycle Status History
                OrderStatusHistory history = new OrderStatusHistory(
                        orderId,
                        null,
                        order.getOrderStatus(),
                        userId,
                        "Order successfully placed with payment method: " + order.getPaymentMethod() + 
                        (cart.getAppliedCouponCode() != null && !cart.getAppliedCouponCode().isEmpty() ? " | Applied Coupon: " + cart.getAppliedCouponCode() : "")
                );
                orderDAO.createStatusHistory(history, conn);

                conn.commit(); // Commit All Changes Atomically
                logger.info("Order [{}] successfully placed for user [{}] with total: {}", orderNumber, userId, order.getTotalAmount());

                // Dispatch Order Confirmation Email Notification
                try {
                    orderDAO.findById(orderId).ifPresent(placedOrder -> {
                        emailService.sendOrderStatusUpdateEmail(
                                placedOrder.getCustomerEmail(),
                                placedOrder.getCustomerName(),
                                placedOrder,
                                placedOrder.getOrderStatus(),
                                "Thank you for your purchase! Your order has been confirmed."
                        );
                    });
                } catch (Exception ex) {
                    logger.warn("Non-fatal: Failed to trigger order confirmation email for orderId: {}", orderId, ex);
                }

                return order;

            } catch (Exception e) {
                conn.rollback();
                logger.error("Checkout transaction failed for user: {}. Rolling back...", userId, e);
                if (e instanceof ValidationException) throw (ValidationException) e;
                throw new DatabaseException("Failed to process checkout transaction", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during checkout", e);
            throw new DatabaseException("Database connection failure", e);
        }
    }

    public Order processCheckout(int userId, int addressId, String paymentMethod, String notes) {
        return processCheckout(userId, addressId, paymentMethod, notes, null);
    }

    /**
     * Executes Atomic Direct "Buy Now" Checkout Transaction for a single product without modifying or clearing the user's cart.
     */
    public Order processDirectBuyCheckout(int userId, int addressId, int productId, int quantity, 
                                          String paymentMethod, String notes, Coupon appliedCoupon) {
        int finalQty = quantity > 0 ? quantity : 1;
        if (finalQty > 3) {
            throw new ValidationException("Purchase limit exceeded: Maximum allowed is 3 units of this product per customer.");
        }

        // 1. Verify Product & Live Stock
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ValidationException("Product not found or unavailable."));

        if (!product.isActive()) {
            throw new ValidationException("Product '" + product.getProductName() + "' is currently unavailable.");
        }

        if (product.getStockQuantity() < finalQty) {
            throw new ValidationException("Insufficient stock available for '" + product.getProductName() + 
                    "'. Available stock: " + product.getStockQuantity());
        }

        // 2. Build standalone Direct Buy Cart for Financials (does NOT affect or modify DB cart)
        Cart directCart = Cart.createDirectBuyCart(userId, product, finalQty);

        if (appliedCoupon != null) {
            BigDecimal couponDiscount = appliedCoupon.calculateDiscount(directCart.getSubtotal());
            directCart.setCouponId(appliedCoupon.getCouponId());
            directCart.setAppliedCouponCode(appliedCoupon.getCode());
            directCart.setCouponDiscount(couponDiscount);
        }

        // 3. Verify Shipping Address
        Address address = addressService.getAddressById(addressId, userId);

        // 4. Prepare Order Entity
        String orderNumber = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + 
                             UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setCouponId(directCart.getCouponId());
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
        order.setNotes(notes);

        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        order.setSubtotal(directCart.getSubtotal());
        order.setDiscountAmount(directCart.getTotalSavings());
        order.setTaxAmount(directCart.getEstimatedTax());
        order.setShippingAmount(BigDecimal.ZERO);
        order.setTotalAmount(directCart.getGrandTotal());

        // Address Details Snapshot
        order.setShippingFullName(address.getFullName());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountry(address.getCountry());
        order.setBillingAddressSnapshot(address.getFormattedAddress());

        // 5. ATOMIC DATABASE TRANSACTION
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Step A: Insert Master Order
                int orderId = orderDAO.createOrder(order, conn);
                order.setOrderId(orderId);

                // Step B: Increment Coupon Usage if applicable
                if (order.getCouponId() != null) {
                    boolean success = couponDAO.incrementUsedCountAtomic(order.getCouponId(), conn);
                    if (!success) {
                        throw new ValidationException("Coupon usage limit has been reached.");
                    }
                }

                // Step C: Insert Single Direct Buy Order Item & Deduct Stock
                CartItem item = directCart.getItems().get(0);
                OrderItem orderItem = new OrderItem(
                        orderId,
                        item.getProductId(),
                        item.getProductName(),
                        item.getSku(),
                        item.getUnitPrice(),
                        item.getDiscountAmount(),
                        item.getLineTax(),
                        item.getLineTotal(),
                        item.getQuantity()
                );

                orderDAO.createOrderItem(orderItem, conn);

                // Deduct Stock atomically with row-level lock
                inventoryService.reserveStockForOrder(item.getProductId(), item.getQuantity(), orderId, conn);

                // Step D: Insert Initial Lifecycle Status History
                OrderStatusHistory history = new OrderStatusHistory(
                        orderId,
                        null,
                        order.getOrderStatus(),
                        userId,
                        "Direct Buy Order placed with payment method: " + order.getPaymentMethod() + 
                        (directCart.getAppliedCouponCode() != null && !directCart.getAppliedCouponCode().isEmpty() ? " | Applied Coupon: " + directCart.getAppliedCouponCode() : "")
                );
                orderDAO.createStatusHistory(history, conn);

                conn.commit(); // Note: User's shopping cart in DB is NOT touched or cleared!
                logger.info("Direct Buy Order [{}] successfully placed for user [{}] with total: {}", orderNumber, userId, order.getTotalAmount());

                // Step F: Asynchronous Customer Email Notification
                try {
                    orderDAO.findById(orderId).ifPresent(placedOrder -> {
                        emailService.sendOrderStatusUpdateEmail(
                                placedOrder.getCustomerEmail(),
                                placedOrder.getCustomerName(),
                                placedOrder,
                                placedOrder.getOrderStatus(),
                                "Thank you for your purchase! Your direct order has been confirmed."
                        );
                    });
                } catch (Exception ex) {
                    logger.warn("Non-fatal: Failed to trigger direct buy confirmation email for orderId: {}", orderId, ex);
                }

                return order;

            } catch (Exception e) {
                conn.rollback();
                logger.error("Direct buy checkout transaction failed for user: {}. Rolling back...", userId, e);
                if (e instanceof ValidationException) throw (ValidationException) e;
                throw new DatabaseException("Failed to process direct buy checkout transaction", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during direct buy checkout", e);
            throw new DatabaseException("Database connection failure", e);
        }
    }

    public Order getOrderById(int orderId, int userId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        if (order.getUserId() != userId) {
            logger.warn("Security Alert: User {} attempted unauthorized access to Order {}", userId, orderId);
            throw new ValidationException("You are not authorized to view this order.");
        }
        return order;
    }

    public Order getOrderByNumber(String orderNumber, int userId) {
        Order order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with Number: " + orderNumber));
        if (order.getUserId() != userId) {
            logger.warn("Security Alert: User {} attempted unauthorized access to Order Number {}", userId, orderNumber);
            throw new ValidationException("You are not authorized to view this order.");
        }
        return order;
    }

    public Order getOrderByIdOrNumber(String idOrNumber, int userId) {
        if (idOrNumber == null || idOrNumber.trim().isEmpty()) {
            throw new ValidationException("Order identifier is required.");
        }
        String clean = idOrNumber.trim();
        if (clean.toUpperCase().startsWith("ORD-") || !clean.matches("\\d+")) {
            return getOrderByNumber(clean, userId);
        }
        try {
            int orderId = Integer.parseInt(clean);
            return getOrderById(orderId, userId);
        } catch (NumberFormatException nfe) {
            return getOrderByNumber(clean, userId);
        }
    }

    public Order getAdminOrderByIdOrNumber(String idOrNumber) {
        if (idOrNumber == null || idOrNumber.trim().isEmpty()) {
            throw new ValidationException("Order identifier is required.");
        }
        String clean = idOrNumber.trim();
        if (clean.toUpperCase().startsWith("ORD-") || !clean.matches("\\d+")) {
            return orderDAO.findByOrderNumber(clean)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with Number: " + clean));
        }
        try {
            int orderId = Integer.parseInt(clean);
            return getAdminOrderById(orderId);
        } catch (NumberFormatException nfe) {
            return orderDAO.findByOrderNumber(clean)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with Number: " + clean));
        }
    }

    public Pagination<Order> getUserOrders(int userId, int page, int pageSize) {
        return orderDAO.findByUserId(userId, null, null, page, pageSize);
    }

    public Pagination<Order> getUserOrders(int userId, String statusFilter, String sortBy, int page, int pageSize) {
        return orderDAO.findByUserId(userId, statusFilter, sortBy, page, pageSize);
    }

    /**
     * Customer cancels an order (restoring inventory and updating status).
     */
    public void cancelOrder(int orderId, int userId, String remarks) {
        Order order = getOrderById(orderId, userId);

        if (!order.isCancellable()) {
            throw new ValidationException("Order cannot be cancelled at current status: " + order.getOrderStatus());
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update Order Status
                orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELLED, conn);

                // 2. Update Payment Status if paid
                if (order.getPaymentStatus() == PaymentStatus.PAID) {
                    orderDAO.updatePaymentStatus(orderId, PaymentStatus.REFUNDED, conn);
                }

                // 3. Restore Product Inventory
                List<OrderItem> items = orderDAO.getOrderItems(orderId, conn);
                for (OrderItem item : items) {
                    inventoryService.restoreStockForCancellation(
                            item.getProductId(),
                            item.getQuantity(),
                            orderId,
                            TransactionType.CANCELLATION,
                            "Cancelled Customer Order #" + orderId,
                            conn
                    );
                }

                if (order.getCouponId() != null && order.getCouponId() > 0) {
                    couponDAO.decrementUsedCount(order.getCouponId(), conn);
                    couponDAO.deleteCouponUsageForOrder(orderId, conn);
                }

                // 4. Record Status History
                OrderStatusHistory history = new OrderStatusHistory(
                        orderId,
                        order.getOrderStatus(),
                        OrderStatus.CANCELLED,
                        userId,
                        remarks != null && !remarks.trim().isEmpty() ? remarks : "Cancelled by Customer"
                );
                orderDAO.createStatusHistory(history, conn);

                conn.commit();
                logger.info("Order [{}] successfully cancelled by user [{}]", orderId, userId);

                // Dispatch Cancellation Email Notification
                try {
                    orderDAO.findById(orderId).ifPresent(cancelledOrder -> {
                        emailService.sendOrderStatusUpdateEmail(
                                cancelledOrder.getCustomerEmail(),
                                cancelledOrder.getCustomerName(),
                                cancelledOrder,
                                OrderStatus.CANCELLED,
                                remarks != null && !remarks.trim().isEmpty() ? remarks : "Order cancelled by customer."
                        );
                    });
                } catch (Exception ex) {
                    logger.warn("Non-fatal: Failed to trigger cancellation email for orderId: {}", orderId, ex);
                }

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error cancelling order: {}", orderId, e);
                throw new DatabaseException("Failed to cancel order", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during order cancellation", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    // Admin Operations
    public Pagination<Order> getAllOrders(String keyword, OrderStatus status, int page, int pageSize) {
        return orderDAO.findAll(keyword, status, page, pageSize);
    }

    public Order getAdminOrderById(int orderId) {
        return orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    public void updateOrderStatusByAdmin(int orderId, OrderStatus newStatus, String remarks, int adminUserId) {
        updateOrderStatusByAdmin(orderId, newStatus, remarks, adminUserId, null, null, null);
    }

    public void updateOrderStatusByAdmin(int orderId, OrderStatus newStatus, String remarks, int adminUserId,
                                         String courierPartner, String trackingNumber, String deliveryAgentPhone) {
        Order order = getAdminOrderById(orderId);
        OrderStatus previousStatus = order.getOrderStatus();

        if (previousStatus == newStatus && courierPartner == null && trackingNumber == null && deliveryAgentPhone == null) {
            return;
        }

        if (!previousStatus.canTransitionTo(newStatus)) {
            throw new ValidationException(String.format(
                    "Invalid fulfillment transition: Cannot move order directly from [%s] to [%s]. " +
                    "Orders must follow sequential fulfillment: Processing -> Dispatched -> In Transit -> Out for Delivery -> Delivered.",
                    previousStatus.getDisplayName(), newStatus.getDisplayName()
            ));
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                orderDAO.updateOrderFulfillment(orderId, newStatus, courierPartner, trackingNumber, deliveryAgentPhone, conn);

                // If admin cancels or marks returned, restore inventory and refund paid order
                if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) {
                    List<OrderItem> items = orderDAO.getOrderItems(orderId, conn);
                    TransactionType transType = newStatus == OrderStatus.RETURNED ? TransactionType.RETURN : TransactionType.CANCELLATION;
                    for (OrderItem item : items) {
                        inventoryService.restoreStockForCancellation(
                                item.getProductId(),
                                item.getQuantity(),
                                orderId,
                                transType,
                                "Admin marked order as " + newStatus,
                                conn
                        );
                    }
                    if (order.getPaymentStatus() == PaymentStatus.PAID) {
                        orderDAO.updatePaymentStatus(orderId, PaymentStatus.REFUNDED, conn);
                    }
                }

                // If Delivered and payment was pending COD, mark paid
                if (newStatus == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.PENDING) {
                    orderDAO.updatePaymentStatus(orderId, PaymentStatus.PAID, conn);
                }

                StringBuilder historyRemarks = new StringBuilder();
                if (remarks != null && !remarks.trim().isEmpty()) {
                    historyRemarks.append(remarks.trim());
                } else {
                    historyRemarks.append("Fulfillment status transitioned to ").append(newStatus.getDisplayName());
                }

                if (courierPartner != null && !courierPartner.trim().isEmpty()) {
                    historyRemarks.append(" | Courier: ").append(courierPartner.trim());
                }
                if (trackingNumber != null && !trackingNumber.trim().isEmpty()) {
                    historyRemarks.append(" (AWB: ").append(trackingNumber.trim()).append(")");
                }
                if (deliveryAgentPhone != null && !deliveryAgentPhone.trim().isEmpty()) {
                    historyRemarks.append(" | Agent Contact: ").append(deliveryAgentPhone.trim());
                }

                OrderStatusHistory history = new OrderStatusHistory(
                        orderId,
                        previousStatus,
                        newStatus,
                        adminUserId,
                        historyRemarks.toString()
                );
                orderDAO.createStatusHistory(history, conn);

                conn.commit();
                logger.info("Admin updated order [{}] status: {} -> {}", orderId, previousStatus, newStatus);

                // Dispatch Order Status Transition Notification Email
                try {
                    orderDAO.findById(orderId).ifPresent(updatedOrder -> {
                        emailService.sendOrderStatusUpdateEmail(
                                updatedOrder.getCustomerEmail(),
                                updatedOrder.getCustomerName(),
                                updatedOrder,
                                newStatus,
                                historyRemarks.toString()
                        );
                    });
                } catch (Exception ex) {
                    logger.warn("Non-fatal: Failed to trigger status transition email for orderId: {}", orderId, ex);
                }

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error updating order status for orderId: {}", orderId, e);
                throw new DatabaseException("Failed to update order status", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during order status update", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    public Map<String, Object> getOrderSummaryStats() {
        return orderDAO.getOrderSummaryStats();
    }

    /**
     * Inspects and cancels expired pending online payment orders (older than maxAgeMinutes),
     * restoring locked stock to the catalog and updating order lifecycle history.
     */
    public int cancelExpiredPendingOrders(int maxAgeMinutes) {
        List<Order> expiredOrders = orderDAO.findExpiredPendingOrders(maxAgeMinutes);
        if (expiredOrders.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Order order : expiredOrders) {
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    orderDAO.updateOrderStatus(order.getOrderId(), OrderStatus.CANCELLED, conn);
                    orderDAO.updatePaymentStatus(order.getOrderId(), PaymentStatus.FAILED, conn);

                    // Restore inventory
                    List<OrderItem> items = orderDAO.getOrderItems(order.getOrderId(), conn);
                    for (OrderItem item : items) {
                        inventoryService.restoreStockForCancellation(
                                item.getProductId(),
                                item.getQuantity(),
                                order.getOrderId(),
                                TransactionType.CANCELLATION,
                                "Automatic cancellation: payment window expired (" + maxAgeMinutes + " mins)",
                                conn
                        );
                    }

                    if (order.getCouponId() != null && order.getCouponId() > 0) {
                        couponDAO.decrementUsedCount(order.getCouponId(), conn);
                        couponDAO.deleteCouponUsageForOrder(order.getOrderId(), conn);
                    }

                    OrderStatusHistory history = new OrderStatusHistory(
                            order.getOrderId(),
                            order.getOrderStatus(),
                            OrderStatus.CANCELLED,
                            null,
                            "Order automatically cancelled: payment window expired after " + maxAgeMinutes + " minutes"
                    );
                    orderDAO.createStatusHistory(history, conn);

                    conn.commit();
                    count++;
                    logger.info("Automatically cancelled expired pending order #{} and released stock.", order.getOrderNumber());
                } catch (Exception e) {
                    conn.rollback();
                    logger.error("Failed to cancel expired pending order #{}", order.getOrderNumber(), e);
                }
            } catch (SQLException e) {
                logger.error("Database connection error while cancelling expired order #{}", order.getOrderNumber(), e);
            }
        }
        return count;
    }
}
