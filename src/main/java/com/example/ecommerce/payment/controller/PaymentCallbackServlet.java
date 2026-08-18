package com.example.ecommerce.payment.controller;

import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.payment.service.PaymentService;
import com.example.ecommerce.payment.service.RazorpayService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller processing payment gateway webhooks and client callbacks.
 * Route: /payment/callback
 * Protected by AuthFilter.
 */
@WebServlet(name = "PaymentCallbackServlet", urlPatterns = {"/payment/callback"})
public class PaymentCallbackServlet extends HttpServlet {

    private PaymentService paymentService;
    private OrderService orderService;
    private CartService cartService;
    private RazorpayService razorpayService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.paymentService = new PaymentService();
        this.orderService = new OrderService();
        this.cartService = new CartService();
        this.razorpayService = new RazorpayService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSession user = (UserSession) session.getAttribute("currentUser");

        int orderId = Integer.parseInt(request.getParameter("orderId"));
        String txnRef = request.getParameter("transactionReference");
        String statusParam = request.getParameter("status");
        String reason = request.getParameter("reason");

        // Razorpay Specific Parameters
        String rzpPaymentId = request.getParameter("razorpay_payment_id");
        String rzpOrderId = request.getParameter("razorpay_order_id");
        String rzpSignature = request.getParameter("razorpay_signature");

        boolean isSuccess;
        String gatewayResponseMsg;

        if (rzpPaymentId != null && !rzpPaymentId.trim().isEmpty()) {
            // Verify HMAC-SHA256 Cryptographic Signature
            boolean isSignatureValid = razorpayService.verifyPaymentSignature(rzpOrderId, rzpPaymentId, rzpSignature);
            if (isSignatureValid) {
                isSuccess = true;
                txnRef = rzpPaymentId;
                gatewayResponseMsg = "Razorpay Payment Confirmed [Payment ID: " + rzpPaymentId + ", Order: " + rzpOrderId + "]";
            } else {
                isSuccess = false;
                gatewayResponseMsg = "Razorpay Signature Verification Failed (Potential Tampering)";
                reason = "Cryptographic signature verification failed. Payment was not recorded.";
            }
        } else {
            // Standard simulated sandbox callback
            isSuccess = "SUCCESS".equalsIgnoreCase(statusParam);
            gatewayResponseMsg = isSuccess ? "Simulated Payment Successful (Txn: " + txnRef + ")" : 
                    (reason != null ? reason : "Payment Declined by Issuer / User Cancelled");
        }

        try {
            boolean processed = paymentService.processSimulatedGatewayCallback(
                    orderId, 
                    user.getUserId(), 
                    txnRef != null ? txnRef : "TXN-" + System.currentTimeMillis(), 
                    rzpOrderId,
                    isSuccess, 
                    gatewayResponseMsg
            );

            Order order = orderService.getOrderById(orderId, user.getUserId());

            if (processed) {
                if (session != null) {
                    Cart freshCart = cartService.getCart(user.getUserId());
                    session.setAttribute("cart", freshCart);
                    session.removeAttribute("appliedCoupon");
                    session.removeAttribute("justPlacedOrder");
                }
                String encodedTxn = txnRef != null ? java.net.URLEncoder.encode(txnRef, java.nio.charset.StandardCharsets.UTF_8) : "";
                response.sendRedirect(request.getContextPath() + "/order?id=" + order.getOrderId() + "&paymentSuccess=true&txnRef=" + encodedTxn);
            } else {
                String errorMsg = reason != null && !reason.trim().isEmpty() ? reason : "Payment was declined by the issuing bank or cancelled. Order has not been placed.";
                String encodedError = java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/payment/gateway?orderId=" + orderId + "&paymentFailed=true&error=" + encodedError);
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/orders");
        }
    }
}
