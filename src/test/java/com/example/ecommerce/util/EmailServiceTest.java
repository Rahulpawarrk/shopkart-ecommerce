package com.example.ecommerce.util;

import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.order.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
    }

    private Order createSampleOrder() {
        Order order = new Order();
        order.setOrderId(42);
        order.setOrderNumber("ORD-20260908-0042");
        order.setUserId(10);
        order.setCustomerName("Rahul Pawar");
        order.setCustomerEmail("rahul@example.com");
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentMethod("UPI");
        order.setSubtotal(new BigDecimal("1299.00"));
        order.setDiscountAmount(new BigDecimal("100.00"));
        order.setTaxAmount(new BigDecimal("60.00"));
        order.setShippingAmount(BigDecimal.ZERO);
        order.setTotalAmount(new BigDecimal("1259.00"));
        order.setShippingFullName("Rahul Pawar");
        order.setShippingPhone("9876543210");
        order.setShippingAddressLine1("Flat 402, Sunshine Heights");
        order.setShippingAddressLine2("MG Road");
        order.setShippingCity("Pune");
        order.setShippingState("Maharashtra");
        order.setShippingPostalCode("411001");

        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = new OrderItem(42, 1, "Wireless Noise-Canceling Headphones", "SKU-HEAD-01",
                new BigDecimal("1299.00"), new BigDecimal("100.00"), new BigDecimal("60.00"),
                new BigDecimal("1259.00"), 1);
        items.add(item1);
        order.setItems(items);

        return order;
    }

    @Test
    @DisplayName("Should generate Order Confirmed email HTML with order details, address, and items")
    void testBuildOrderConfirmedEmailHtml() {
        Order order = createSampleOrder();
        String html = emailService.buildOrderConfirmedEmailHtml("Rahul Pawar", order, "Thank you!");

        assertNotNull(html);
        assertTrue(html.contains("ORD-20260908-0042"));
        assertTrue(html.contains("Rahul Pawar"));
        assertTrue(html.contains("Wireless Noise-Canceling Headphones"));
        assertTrue(html.contains("1259.00"));
        assertTrue(html.contains("Pune, Maharashtra - 411001"));
        assertTrue(html.contains("Track Your Order"));
        assertTrue(html.contains("Order Confirmed"));
    }

    @Test
    @DisplayName("Should generate Order Cancelled email HTML with refund information for prepaid order")
    void testBuildOrderCancelledEmailHtmlPrepaid() {
        Order order = createSampleOrder();
        order.setOrderStatus(OrderStatus.CANCELLED);
        String html = emailService.buildOrderCancelledEmailHtml("Rahul Pawar", order, "Changed delivery address");

        assertNotNull(html);
        assertTrue(html.contains("ORD-20260908-0042"));
        assertTrue(html.contains("Order Cancelled"));
        assertTrue(html.contains("Changed delivery address"));
        assertTrue(html.contains("Refund Initiated"));
        assertTrue(html.contains("5 to 7 business days"));
    }

    @Test
    @DisplayName("Should generate Order Cancelled email HTML with COD information when COD")
    void testBuildOrderCancelledEmailHtmlCOD() {
        Order order = createSampleOrder();
        order.setPaymentMethod("COD");
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.CANCELLED);

        String html = emailService.buildOrderCancelledEmailHtml("Rahul Pawar", order, "Cancelled by user");

        assertNotNull(html);
        assertTrue(html.contains("Cash on Delivery (COD)"));
        assertTrue(html.contains("no payment was collected"));
    }

    @Test
    @DisplayName("Should generate Return email HTML for Return Request")
    void testBuildOrderReturnEmailHtmlRequested() {
        OrderReturn ret = new OrderReturn();
        ret.setReturnNumber("RET-20260908-ORD42-ABCD");
        ret.setOrderNumber("ORD-20260908-0042");
        ret.setReturnReason("Defective / Damaged product");
        ret.setResolutionType("REFUND");
        ret.setReturnStatus("REQUESTED");
        ret.setComments("Right earcup not producing sound");

        String html = emailService.buildOrderReturnEmailHtml("Rahul Pawar", ret, "Under review");

        assertNotNull(html);
        assertTrue(html.contains("RET-20260908-ORD42-ABCD"));
        assertTrue(html.contains("ORD-20260908-0042"));
        assertTrue(html.contains("Defective / Damaged product"));
        assertTrue(html.contains("REFUND"));
        assertTrue(html.contains("Right earcup not producing sound"));
        assertTrue(html.contains("We Have Received Your Return Request"));
    }

    @Test
    @DisplayName("Should generate Return email HTML for Approved and Refunded status")
    void testBuildOrderReturnEmailHtmlRefunded() {
        OrderReturn ret = new OrderReturn();
        ret.setReturnNumber("RET-20260908-ORD42-ABCD");
        ret.setOrderNumber("ORD-20260908-0042");
        ret.setReturnReason("Incorrect Size");
        ret.setResolutionType("REFUND");
        ret.setReturnStatus("REFUNDED");
        ret.setRefundAmount(new BigDecimal("1259.00"));
        ret.setAdminNotes("Quality check passed, refund processed to original source");

        String html = emailService.buildOrderReturnEmailHtml("Rahul Pawar", ret, "Refunded");

        assertNotNull(html);
        assertTrue(html.contains("Refund Processed Successfully"));
        assertTrue(html.contains("1259.00"));
        assertTrue(html.contains("Quality check passed"));
    }

    @Test
    @DisplayName("Should build appropriate subjects for return statuses")
    void testBuildOrderReturnSubject() {
        assertEquals("↩ Return Request Received: #RET-01 (Order #ORD-99) | ShopKart",
                emailService.buildOrderReturnSubject("RET-01", "ORD-99", "REQUESTED"));
        assertEquals("✓ Return Request Approved: #RET-01 (Order #ORD-99) | ShopKart",
                emailService.buildOrderReturnSubject("RET-01", "ORD-99", "APPROVED"));
        assertEquals("💰 Refund Processed for Return #RET-01 (Order #ORD-99) | ShopKart",
                emailService.buildOrderReturnSubject("RET-01", "ORD-99", "REFUNDED"));
    }

    @Test
    @DisplayName("Should route buildOrderStatusEmailHtml correctly according to status")
    void testBuildOrderStatusEmailHtmlRouting() {
        Order order = createSampleOrder();
        
        String confirmedHtml = emailService.buildOrderStatusEmailHtml("Rahul", order, OrderStatus.CONFIRMED, null);
        assertTrue(confirmedHtml.contains("Order Confirmed"));

        String cancelledHtml = emailService.buildOrderStatusEmailHtml("Rahul", order, OrderStatus.CANCELLED, "Changed mind");
        assertTrue(cancelledHtml.contains("Order Cancelled"));

        String dispatchedHtml = emailService.buildOrderStatusEmailHtml("Rahul", order, OrderStatus.DISPATCHED, null);
        assertTrue(dispatchedHtml.contains("Dispatched") || dispatchedHtml.contains("on the Way"));
    }
}