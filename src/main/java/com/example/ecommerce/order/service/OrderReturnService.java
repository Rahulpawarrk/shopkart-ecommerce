package com.example.ecommerce.order.service;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.dao.OrderReturnDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.util.EmailService;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer for Order Returns, Replacements, and Refund Resolutions.
 */
@Service
@Transactional
public class OrderReturnService {

    private static final Logger logger = LoggerFactory.getLogger(OrderReturnService.class);

    private final OrderReturnDAO orderReturnDAO;
    private final OrderDAO orderDAO;
    private final EmailService emailService;

    public OrderReturnService() {
        this(new OrderReturnDAO(), new OrderDAO(), new EmailService());
    }

    public OrderReturnService(OrderReturnDAO orderReturnDAO, OrderDAO orderDAO) {
        this(orderReturnDAO, orderDAO, new EmailService());
    }

    public OrderReturnService(OrderReturnDAO orderReturnDAO, OrderDAO orderDAO, EmailService emailService) {
        this.orderReturnDAO = orderReturnDAO;
        this.orderDAO = orderDAO;
        this.emailService = emailService != null ? emailService : new EmailService();
    }

    /**
     * Submits a customer return request for a delivered order.
     */
    public OrderReturn requestReturn(int userId, int orderId, String reason, String resolutionType, String comments, String imageUrl) {
        List<String> errors = new ArrayList<>();
        String cleanReason = reason != null ? reason.trim() : "";
        if (cleanReason.isEmpty()) {
            errors.add("Please select a valid return reason.");
        }
        String cleanResolution = (resolutionType != null && !resolutionType.trim().isEmpty())
                ? resolutionType.trim().toUpperCase()
                : "REFUND";
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // 1. Verify Order Exists and belongs to User
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getUserId() != userId) {
            throw new ValidationException("You are not authorized to return this order.");
        }

        // 2. Enforce Delivery Condition
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new ValidationException("Returns can only be requested after the order has been delivered.");
        }

        // 3. Check for Duplicate Return Request
        Optional<OrderReturn> existing = orderReturnDAO.findByOrderId(orderId);
        if (existing.isPresent()) {
            throw new ValidationException("A return request has already been submitted for this order (Ref: " + existing.get().getReturnNumber() + ").");
        }

        // 4. Generate Unique Return Number
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String returnNumber = "RET-" + datePart + "-ORD" + orderId + "-" + randPart;

        OrderReturn orderReturn = new OrderReturn(orderId, userId, returnNumber, cleanReason, cleanResolution, comments != null ? comments.trim() : null, imageUrl);
        int returnId = orderReturnDAO.createReturn(orderReturn);
        orderReturn.setReturnId(returnId);
        orderReturn.setOrderNumber(order.getOrderNumber());

        logger.info("Created return request [returnId={}, returnNumber={}] for orderId [{}] by userId [{}]", 
                returnId, returnNumber, orderId, userId);

        // Dispatch Customer Return Request Email Notification
        try {
            String custEmail = (order.getCustomerEmail() != null && !order.getCustomerEmail().trim().isEmpty())
                    ? order.getCustomerEmail()
                    : orderReturn.getCustomerEmail();
            String custName = (order.getCustomerName() != null && !order.getCustomerName().trim().isEmpty())
                    ? order.getCustomerName()
                    : orderReturn.getCustomerName();
            if (custEmail != null && !custEmail.trim().isEmpty()) {
                emailService.sendOrderReturnEmail(
                        custEmail,
                        custName,
                        orderReturn,
                        "Return request submitted by customer."
                );
            }
        } catch (Exception ex) {
            logger.warn("Non-fatal: Failed to send return request confirmation email for returnId: {}", returnId, ex);
        }

        return orderReturn;
    }

    public Optional<OrderReturn> getReturnByOrderId(int orderId) {
        return orderReturnDAO.findByOrderId(orderId);
    }

    public Optional<OrderReturn> getReturnById(int returnId) {
        return orderReturnDAO.findById(returnId);
    }

    public Pagination<OrderReturn> getUserReturns(int userId, int page, int pageSize) {
        return orderReturnDAO.findByUserId(userId, page, pageSize);
    }

    public Pagination<OrderReturn> getAllReturns(String keyword, String status, int page, int pageSize) {
        return orderReturnDAO.findAll(keyword, status, page, pageSize);
    }

    public boolean updateReturnStatus(int returnId, String newStatus, String adminNotes, BigDecimal refundAmount) {
        boolean updated = orderReturnDAO.updateStatus(returnId, newStatus, adminNotes, refundAmount);
        if (updated) {
            String normStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";
            // When return is approved, refunded, or completed, synchronize order status to RETURNED and restore inventory
            if ("APPROVED".equals(normStatus) || "REFUNDED".equals(normStatus) || "COMPLETED".equals(normStatus) || "RETURNED".equals(normStatus)) {
                try {
                    orderReturnDAO.findById(returnId).ifPresent(ret -> {
                        int orderId = ret.getOrderId();
                        try (java.sql.Connection conn = com.example.ecommerce.config.DBConnection.getConnection()) {
                            conn.setAutoCommit(false);
                            try {
                                Optional<Order> optOrder = orderDAO.findById(orderId);
                                if (optOrder.isPresent()) {
                                    Order order = optOrder.get();
                                    if (order.getOrderStatus() != OrderStatus.RETURNED) {
                                        orderDAO.updateOrderStatus(orderId, OrderStatus.RETURNED, conn);
                                        if (order.getPaymentStatus() == com.example.ecommerce.order.model.PaymentStatus.PAID || refundAmount != null) {
                                            orderDAO.updatePaymentStatus(orderId, com.example.ecommerce.order.model.PaymentStatus.REFUNDED, conn);
                                        }

                                        com.example.ecommerce.inventory.service.InventoryService invService = new com.example.ecommerce.inventory.service.InventoryService();
                                        List<com.example.ecommerce.order.model.OrderItem> items = orderDAO.getOrderItems(orderId, conn);
                                        for (com.example.ecommerce.order.model.OrderItem item : items) {
                                            invService.restoreStockForCancellation(
                                                    item.getProductId(),
                                                    item.getQuantity(),
                                                    orderId,
                                                    com.example.ecommerce.inventory.model.TransactionType.RETURN,
                                                    "Return processed: " + ret.getReturnNumber(),
                                                    conn
                                            );
                                        }

                                        com.example.ecommerce.order.model.OrderStatusHistory history = new com.example.ecommerce.order.model.OrderStatusHistory(
                                                orderId,
                                                order.getOrderStatus(),
                                                OrderStatus.RETURNED,
                                                ret.getUserId(),
                                                "Return " + normStatus + " by Admin (Ref: " + ret.getReturnNumber() + ")"
                                        );
                                        orderDAO.createStatusHistory(history, conn);
                                    }
                                }
                                conn.commit();
                            } catch (Exception ex) {
                                conn.rollback();
                                logger.error("Error processing return order sync for orderId: {}", orderId, ex);
                            }
                        } catch (java.sql.SQLException e) {
                            logger.error("Database connection error during return sync", e);
                        }
                    });
                } catch (Exception ex) {
                    logger.warn("Non-fatal: Return status updated, secondary order sync error", ex);
                }
            }
            logger.info("Admin updated returnId [{}] status to: {}", returnId, newStatus);

            // Dispatch Customer Return Status Update Email Notification
            try {
                orderReturnDAO.findById(returnId).ifPresent(ret -> {
                    String custEmail = ret.getCustomerEmail();
                    String custName = ret.getCustomerName();
                    if (custEmail != null && !custEmail.trim().isEmpty()) {
                        emailService.sendOrderReturnEmail(
                                custEmail,
                                custName,
                                ret,
                                adminNotes != null && !adminNotes.trim().isEmpty() ? adminNotes.trim() : "Return status updated to " + normStatus
                        );
                    }
                });
            } catch (Exception ex) {
                logger.warn("Non-fatal: Failed to send return status update email for returnId: {}", returnId, ex);
            }
        }
        return updated;
    }
}
