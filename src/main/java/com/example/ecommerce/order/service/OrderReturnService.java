package com.example.ecommerce.order.service;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.dao.OrderReturnDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
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

/**
 * Service Layer for Order Returns, Replacements, and Refund Resolutions.
 */
public class OrderReturnService {

    private static final Logger logger = LoggerFactory.getLogger(OrderReturnService.class);

    private final OrderReturnDAO orderReturnDAO;
    private final OrderDAO orderDAO;

    public OrderReturnService() {
        this.orderReturnDAO = new OrderReturnDAO();
        this.orderDAO = new OrderDAO();
    }

    public OrderReturnService(OrderReturnDAO orderReturnDAO, OrderDAO orderDAO) {
        this.orderReturnDAO = orderReturnDAO;
        this.orderDAO = orderDAO;
    }

    /**
     * Submits a customer return request for a delivered order.
     */
    public OrderReturn requestReturn(int userId, int orderId, String reason, String resolutionType, String comments, String imageUrl) {
        List<String> errors = new ArrayList<>();
        if (reason == null || reason.trim().isEmpty()) {
            errors.add("Please select a valid return reason.");
        }
        if (resolutionType == null || resolutionType.trim().isEmpty()) {
            resolutionType = "REFUND";
        }
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

        OrderReturn orderReturn = new OrderReturn(orderId, userId, returnNumber, reason.trim(), resolutionType.trim().toUpperCase(), comments != null ? comments.trim() : null, imageUrl);
        int returnId = orderReturnDAO.createReturn(orderReturn);
        orderReturn.setReturnId(returnId);
        orderReturn.setOrderNumber(order.getOrderNumber());

        logger.info("Created return request [returnId={}, returnNumber={}] for orderId [{}] by userId [{}]", 
                returnId, returnNumber, orderId, userId);

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
            logger.info("Admin updated returnId [{}] status to: {}", returnId, newStatus);
        }
        return updated;
    }
}
