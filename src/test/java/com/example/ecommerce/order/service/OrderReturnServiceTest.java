package com.example.ecommerce.order.service;

import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.dao.OrderReturnDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderReturn;
import com.example.ecommerce.order.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReturnService Unit Tests with Mockito")
class OrderReturnServiceTest {

    @Mock
    private OrderReturnDAO orderReturnDAO;

    @Mock
    private OrderDAO orderDAO;

    @InjectMocks
    private OrderReturnService orderReturnService;

    @Test
    @DisplayName("Should successfully create return request when order is delivered")
    void testRequestReturnSuccess() {
        Order order = new Order();
        order.setOrderId(101);
        order.setUserId(5);
        order.setOrderNumber("ORD-20260818-101");
        order.setOrderStatus(OrderStatus.DELIVERED);

        when(orderDAO.findById(101)).thenReturn(Optional.of(order));
        when(orderReturnDAO.findByOrderId(101)).thenReturn(Optional.empty());
        when(orderReturnDAO.createReturn(any(OrderReturn.class))).thenReturn(501);

        OrderReturn ret = orderReturnService.requestReturn(
                5, 101, "Defective / Damaged product received", "REFUND", "Screen has dead pixels", "/uploads/returns/proof.jpg"
        );

        assertNotNull(ret);
        assertEquals(501, ret.getReturnId());
        assertEquals("ORD-20260818-101", ret.getOrderNumber());
        assertEquals("REFUND", ret.getResolutionType());
        assertTrue(ret.getReturnNumber().startsWith("RET-"));
        verify(orderReturnDAO, times(1)).createReturn(any(OrderReturn.class));
    }

    @Test
    @DisplayName("Should reject return request if order is not delivered")
    void testRequestReturnNonDeliveredOrderFails() {
        Order order = new Order();
        order.setOrderId(102);
        order.setUserId(5);
        order.setOrderStatus(OrderStatus.IN_TRANSIT);

        when(orderDAO.findById(102)).thenReturn(Optional.of(order));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                orderReturnService.requestReturn(5, 102, "Changed mind", "REFUND", "No notes", null)
        );

        assertTrue(ex.getMessage().contains("delivered"));
        verify(orderReturnDAO, never()).createReturn(any());
    }

    @Test
    @DisplayName("Should reject return request if unauthorized user attempts return")
    void testRequestReturnUnauthorizedUserFails() {
        Order order = new Order();
        order.setOrderId(103);
        order.setUserId(10); // Belongs to user 10
        order.setOrderStatus(OrderStatus.DELIVERED);

        when(orderDAO.findById(103)).thenReturn(Optional.of(order));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                orderReturnService.requestReturn(5, 103, "Damaged", "REFUND", null, null) // User 5 attempts
        );

        assertTrue(ex.getMessage().contains("authorized"));
        verify(orderReturnDAO, never()).createReturn(any());
    }

    @Test
    @DisplayName("Should reject duplicate return request for the same order")
    void testRequestReturnDuplicateFails() {
        Order order = new Order();
        order.setOrderId(104);
        order.setUserId(5);
        order.setOrderStatus(OrderStatus.DELIVERED);

        OrderReturn existing = new OrderReturn();
        existing.setReturnNumber("RET-20260818-ORD104-ABCD");

        when(orderDAO.findById(104)).thenReturn(Optional.of(order));
        when(orderReturnDAO.findByOrderId(104)).thenReturn(Optional.of(existing));

        ValidationException ex = assertThrows(ValidationException.class, () ->
                orderReturnService.requestReturn(5, 104, "Damaged", "REPLACEMENT", null, null)
        );

        assertTrue(ex.getMessage().contains("already been submitted"));
        verify(orderReturnDAO, never()).createReturn(any());
    }

    @Test
    @DisplayName("Should successfully update return status by admin")
    void testUpdateReturnStatusSuccess() {
        when(orderReturnDAO.updateStatus(501, "APPROVED", "Scheduled pickup via BlueDart", BigDecimal.valueOf(1500.00)))
                .thenReturn(true);

        boolean updated = orderReturnService.updateReturnStatus(501, "APPROVED", "Scheduled pickup via BlueDart", BigDecimal.valueOf(1500.00));
        assertTrue(updated);
    }
}
