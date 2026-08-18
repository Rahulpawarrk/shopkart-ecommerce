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

/**
 * Service Layer orchestrating Payment Gateway simulations, COD verifications, and audit logging.
 */
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

    public PaymentService(PaymentDAO paymentDAO, OrderDAO orderDAO, PaymentReconciliationDAO paymentReconciliationDAO) {
        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
        this.paymentReconciliationDAO = paymentReconciliationDAO;
    }

    /**
     * Initializes a payment record for an order.
     */
    public Payment initiatePayment(Order order, String paymentMethod) {
        String txnRef = "PAY-" + paymentMethod.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        
        PaymentTransactionStatus initialStatus = "COD".equalsIgnoreCase(paymentMethod) ? 
                PaymentTransactionStatus.PENDING : PaymentTransactionStatus.PENDING;

        Payment payment = new Payment(
                order.getOrderId(),
                paymentMethod,
                txnRef,
                order.getTotalAmount(),
                initialStatus,
                "Initialized payment intent via " + paymentMethod
        );

        int paymentId = paymentDAO.createPayment(payment);
        payment.setPaymentId(paymentId);
        logger.info("Initiated payment [{}] for Order ID: {} with amount: {}", txnRef, order.getOrderId(), order.getTotalAmount());
        return payment;
    }

    /**
     * Processes simulated gateway callback (Success or Failure).
     */
    public boolean processSimulatedGatewayCallback(int orderId, int userId, String transactionReference, 
                                                   boolean isSuccess, String gatewayResponse) {
        return processSimulatedGatewayCallback(orderId, userId, transactionReference, null, isSuccess, gatewayResponse);
    }

    /**
     * Processes simulated gateway callback with Gateway Order ID (e.g. Razorpay order_id).
     */
    public boolean processSimulatedGatewayCallback(int orderId, int userId, String transactionReference, 
                                                   String gatewayOrderId, boolean isSuccess, String gatewayResponse) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getUserId() != userId) {
            throw new ValidationException("You are not authorized to process payment for this order.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isSuccess) {
                    paymentDAO.updatePaymentStatusByOrderId(orderId, PaymentTransactionStatus.SUCCESS, gatewayResponse, conn);
                    orderDAO.updatePaymentStatus(orderId, PaymentStatus.PAID, conn);

                    // Preserve existing fulfillment status if order is already placed/in-transit (e.g. COD orders being paid online)
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
                    logger.info("Payment SUCCESS confirmed for order [{}] (Txn: {}, Fulfillment Status preserved: {})", orderId, transactionReference, targetStatus);
                } else {
                    paymentDAO.updatePaymentStatusByOrderId(orderId, PaymentTransactionStatus.FAILED, gatewayResponse, conn);

                    OrderStatus currentStatus = order.getOrderStatus();
                    if (currentStatus == OrderStatus.PENDING) {
                        // Initial checkout payment failure: do not place order, mark cancelled
                        orderDAO.updatePaymentStatus(orderId, PaymentStatus.FAILED, conn);
                        orderDAO.updateOrderStatus(orderId, OrderStatus.CANCELLED, conn);

                        OrderStatusHistory history = new OrderStatusHistory(
                                orderId,
                                currentStatus,
                                OrderStatus.CANCELLED,
                                userId,
                                "Payment failed via Gateway (Reason: " + gatewayResponse + ") - Order not placed"
                        );
                        orderDAO.createStatusHistory(history, conn);
                    } else {
                        // Placed COD order online payment attempt failed: keep order active with PENDING payment
                        orderDAO.updatePaymentStatus(orderId, PaymentStatus.PENDING, conn);

                        OrderStatusHistory history = new OrderStatusHistory(
                                orderId,
                                currentStatus,
                                currentStatus,
                                userId,
                                "Online payment attempt failed (Reason: " + gatewayResponse + ") - Order remains active for COD / retry"
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
