package com.example.ecommerce.payment.service;

import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.OrderStatusHistory;
import com.example.ecommerce.order.model.PaymentStatus;
import com.example.ecommerce.payment.dao.PaymentDAO;
import com.example.ecommerce.payment.dao.PaymentReconciliationDAO;
import com.example.ecommerce.payment.model.Payment;
import com.example.ecommerce.payment.model.PaymentReconciliation;
import com.example.ecommerce.payment.model.PaymentTransactionStatus;
import com.example.ecommerce.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer orchestrating Payment Gateway integrations, COD verifications, idempotency, and audit logging.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final PaymentReconciliationDAO paymentReconciliationDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.orderDAO = new OrderDAO();
        this.paymentReconciliationDAO = new PaymentReconciliationDAO();
    }

    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
        this.paymentReconciliationDAO = new PaymentReconciliationDAO();
    }

    @Autowired
    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO, PaymentReconciliationDAO paymentReconciliationDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
        this.paymentReconciliationDAO = paymentReconciliationDAO;
    }

    /**
     * Initializes a payment record for an order.
     */
    public Payment initiatePayment(Order order, String paymentMethod) {
        String method = (paymentMethod != null && !paymentMethod.trim().isEmpty()) ? paymentMethod.trim().toUpperCase() : "ONLINE";
        String txnRef = "PAY-" + method + "-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment payment = new Payment(
                order.getOrderId(),
                method,
                txnRef,
                order.getTotalAmount(),
                PaymentTransactionStatus.PENDING,
                "Initialized payment intent via " + method
        );

        int paymentId = paymentDAO.createPayment(payment);
        payment.setPaymentId(paymentId);
        logger.info("Initiated payment [{}] for Order ID: {} with amount: ₹{}", txnRef, order.getOrderId(), order.getTotalAmount());
        return payment;
    }

    /**
     * Processes gateway callback (Success or Failure) with idempotency and database transaction safety.
     */
    public boolean processGatewayCallback(int orderId, int userId, String transactionReference, 
                                          String gatewayOrderId, boolean isSuccess, String gatewayResponse) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getUserId() != userId) {
            throw new ValidationException("You are not authorized to process payment for this order.");
        }

        // Idempotency check: If order is already paid, do not re-process
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            logger.info("Order ID #{} is already in PAID status. Idempotent callback accepted.", orderId);
            return true;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isSuccess) {
                    paymentDAO.updatePaymentStatusByOrderId(orderId, PaymentTransactionStatus.SUCCESS, gatewayResponse, conn);
                    orderDAO.updatePaymentStatus(orderId, PaymentStatus.PAID, conn);

                    // Transition to CONFIRMED if in PENDING state
                    OrderStatus currentStatus = order.getOrderStatus();
                    OrderStatus targetStatus = currentStatus;

                    if (currentStatus == OrderStatus.PENDING) {
                        targetStatus = OrderStatus.CONFIRMED;
                        orderDAO.updateOrderStatus(orderId, targetStatus, conn);
                    }

                    OrderStatusHistory history = new OrderStatusHistory(
                            orderId,
                            currentStatus,
                            targetStatus,
                            userId,
                            "Payment successfully confirmed via Gateway (Txn Ref: " + transactionReference + ")"
                    );
                    orderDAO.createStatusHistory(history, conn);
                    logger.info("Payment SUCCESS recorded for order [{}] (Txn: {}, Target Status: {})", orderId, transactionReference, targetStatus);
                } else {
                    paymentDAO.updatePaymentStatusByOrderId(orderId, PaymentTransactionStatus.FAILED, gatewayResponse, conn);

                    OrderStatus currentStatus = order.getOrderStatus();
                    if (currentStatus == OrderStatus.PENDING) {
                        orderDAO.updatePaymentStatus(orderId, PaymentStatus.FAILED, conn);
                        // Do not immediately cancel if retry is allowed; or mark CANCELLED with ability to retry
                        orderDAO.updateOrderStatus(orderId, OrderStatus.PENDING, conn);

                        OrderStatusHistory history = new OrderStatusHistory(
                                orderId,
                                currentStatus,
                                currentStatus,
                                userId,
                                "Payment failed via Gateway (Reason: " + gatewayResponse + ") - Awaiting customer retry or COD switch"
                        );
                        orderDAO.createStatusHistory(history, conn);
                    }

                    // Log into dedicated payment_reconciliation table for Admin audit & customer query resolution
                    PaymentReconciliation recon = new PaymentReconciliation(
                            orderId,
                            userId,
                            transactionReference != null ? transactionReference : "TXN-FAILED-" + System.currentTimeMillis(),
                            gatewayOrderId,
                            order.getPaymentMethod() != null ? order.getPaymentMethod() : "ONLINE",
                            order.getTotalAmount(),
                            gatewayResponse != null ? gatewayResponse : "Transaction Declined or Cancelled",
                            gatewayResponse
                    );
                    paymentReconciliationDAO.createRecord(recon, conn);

                    logger.warn("Payment FAILED for order [{}] (Txn: {}), logged in payment_reconciliation table", orderId, transactionReference);
                }

                conn.commit();
                return isSuccess;

            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error processing gateway callback for order: {}", orderId, e);
                throw new DatabaseException("Failed to process payment callback", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error during payment callback", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    /**
     * Backward-compatible alias for processGatewayCallback.
     */
    public boolean processSimulatedGatewayCallback(int orderId, int userId, String transactionReference, 
                                                   String gatewayOrderId, boolean isSuccess, String gatewayResponse) {
        return processGatewayCallback(orderId, userId, transactionReference, gatewayOrderId, isSuccess, gatewayResponse);
    }

    /**
     * Records a payment failure or cancellation event safely for an order.
     */
    public boolean handlePaymentFailure(int orderId, String reason) {
        Order order = orderDAO.findById(orderId).orElse(null);
        if (order == null) return false;
        return processGatewayCallback(orderId, order.getUserId(), "TXN-CANCELLED-" + System.currentTimeMillis(), null, false, reason);
    }

    public boolean handlePaymentFailure(int orderId, int userId, String reason) {
        return processGatewayCallback(orderId, userId, "TXN-CANCELLED-" + System.currentTimeMillis(), null, false, reason);
    }

    public boolean processSimulatedGatewayCallback(int orderId, int userId, String transactionReference, 
                                                   boolean isSuccess, String gatewayResponse) {
        return processGatewayCallback(orderId, userId, transactionReference, null, isSuccess, gatewayResponse);
    }

    /**
     * Switches an order's payment method to Cash on Delivery (COD) following an online payment failure.
     */
    public boolean switchPaymentMethodToCod(int orderId, int userId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getUserId() != userId) {
            throw new ValidationException("Unauthorized access to order.");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Order is already paid.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                orderDAO.updateOrderStatus(orderId, OrderStatus.CONFIRMED, conn);
                orderDAO.updatePaymentStatus(orderId, PaymentStatus.PENDING, conn);

                OrderStatusHistory history = new OrderStatusHistory(
                        orderId,
                        order.getOrderStatus(),
                        OrderStatus.CONFIRMED,
                        userId,
                        "Customer switched payment method to Cash on Delivery (COD)"
                );
                orderDAO.createStatusHistory(history, conn);

                conn.commit();
                logger.info("Order [{}] payment method successfully switched to COD for user {}", orderId, userId);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error switching order [{}] to COD", orderId, e);
                throw new DatabaseException("Failed to switch order to COD", e);
            }
        } catch (SQLException e) {
            logger.error("Database connection error while switching to COD", e);
            throw new DatabaseException("Database connection error", e);
        }
    }

    public List<Payment> getOrderPayments(int orderId, int userId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (order.getUserId() != userId) {
            throw new ValidationException("Unauthorized access to payment records.");
        }
        return paymentDAO.findByOrderId(orderId);
    }

    public Pagination<Payment> getAllPayments(String keyword, int page, int pageSize) {
        return paymentDAO.findAll(keyword, page, pageSize);
    }

    public Map<String, Object> getPaymentSummaryStats() {
        return paymentDAO.getPaymentSummaryStats();
    }

    // ==========================================
    // PAYMENT RECONCILIATION METHODS FOR ADMIN
    // ==========================================

    public Pagination<PaymentReconciliation> getReconciliationList(String keyword, String status, int page, int pageSize) {
        return paymentReconciliationDAO.findAll(keyword, status, page, pageSize);
    }

    public Optional<PaymentReconciliation> getReconciliationById(int reconciliationId) {
        return paymentReconciliationDAO.findById(reconciliationId);
    }

    public boolean updateReconciliationResolution(int reconciliationId, String newStatus, String adminNotes, int adminUserId) {
        return paymentReconciliationDAO.updateResolution(reconciliationId, newStatus, adminNotes, adminUserId);
    }

    public Map<String, Object> getReconciliationStats() {
        return paymentReconciliationDAO.getReconciliationStats();
    }
}
