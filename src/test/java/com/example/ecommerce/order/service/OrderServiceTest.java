package com.example.ecommerce.order.service;

import com.example.ecommerce.cart.dao.CartDAO;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.customer.service.AddressService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.service.InventoryService;
import com.example.ecommerce.order.dao.OrderDAO;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import com.example.ecommerce.util.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests with Mockito")
class OrderServiceTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CartService cartService;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private AddressService addressService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private EmailService emailService;

    @Mock
    private com.example.ecommerce.product.dao.ProductDAO productDAO;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should reject checkout when shopping cart is empty")
    void testProcessCheckoutEmptyCart() {
        Cart emptyCart = new Cart(1, 10); // 0 items
        when(cartService.getCart(10)).thenReturn(emptyCart);

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.processCheckout(10, 1, "COD", "Leave at door")
        );

        assertTrue(ex.getMessage().contains("Shopping cart is empty"));
    }

    @Test
    @DisplayName("Should reject cancelling an order that has already been delivered")
    void testCancelOrderDeliveredThrowsException() {
        Order deliveredOrder = new Order();
        deliveredOrder.setOrderId(101);
        deliveredOrder.setUserId(10);
        deliveredOrder.setOrderStatus(OrderStatus.DELIVERED); // Delivered is not cancellable

        when(orderDAO.findById(101)).thenReturn(Optional.of(deliveredOrder));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.cancelOrder(101, 10, "Customer changed mind")
        );

        assertTrue(ex.getMessage().contains("cannot be cancelled at current status"));
    }

    @Test
    @DisplayName("Should reject unauthorized order access across different user IDs (IDOR check)")
    void testUnauthorizedOrderAccess() {
        Order user1Order = new Order();
        user1Order.setOrderId(50);
        user1Order.setUserId(1); // User 1 owns this order

        when(orderDAO.findById(50)).thenReturn(Optional.of(user1Order));

        // User 2 attempts to fetch User 1's order
        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.getOrderById(50, 2)
        );

        assertTrue(ex.getMessage().contains("not authorized"));
    }

    @Test
    @DisplayName("Should reject invalid direct transition from PROCESSING to OUT_FOR_DELIVERY without DISPATCHED/IN_TRANSIT")
    void testSequentialFulfillmentValidationRejectsSkippingDispatch() {
        Order processingOrder = new Order();
        processingOrder.setOrderId(200);
        processingOrder.setUserId(5);
        processingOrder.setOrderStatus(OrderStatus.PROCESSING);

        when(orderDAO.findById(200)).thenReturn(Optional.of(processingOrder));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.updateOrderStatusByAdmin(200, OrderStatus.OUT_FOR_DELIVERY, "Skipping dispatch", 1)
        );

        assertTrue(ex.getMessage().contains("Invalid fulfillment transition"));
    }

    @Test
    @DisplayName("Should reject invalid direct transition from CONFIRMED to DELIVERED")
    void testSequentialFulfillmentValidationRejectsSkippingToDelivered() {
        Order confirmedOrder = new Order();
        confirmedOrder.setOrderId(201);
        confirmedOrder.setUserId(5);
        confirmedOrder.setOrderStatus(OrderStatus.CONFIRMED);

        when(orderDAO.findById(201)).thenReturn(Optional.of(confirmedOrder));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.updateOrderStatusByAdmin(201, OrderStatus.DELIVERED, "Direct deliver", 1)
        );

        assertTrue(ex.getMessage().contains("Invalid fulfillment transition"));
    }

    @Test
    @DisplayName("Should reject Direct Buy when product stock is insufficient")
    void testDirectBuyRejectsOutOfStockProduct() {
        com.example.ecommerce.product.model.Product product = new com.example.ecommerce.product.model.Product();
        product.setProductId(88);
        product.setProductName("Gaming Laptop");
        product.setStatus("ACTIVE");
        product.setStockQuantity(2);

        when(productDAO.findById(88)).thenReturn(Optional.of(product));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.processDirectBuyCheckout(10, 1, 88, 5, "COD", "Urgent delivery", null)
        );

        assertTrue(ex.getMessage().contains("Insufficient stock available"));
        verifyNoInteractions(cartService);
        verifyNoInteractions(cartDAO);
    }

    @Test
    @DisplayName("Should reject Direct Buy when product is inactive / disabled")
    void testDirectBuyRejectsInactiveProduct() {
        com.example.ecommerce.product.model.Product product = new com.example.ecommerce.product.model.Product();
        product.setProductId(89);
        product.setProductName("Discontinued Phone");
        product.setStatus("INACTIVE");
        product.setStockQuantity(10);

        when(productDAO.findById(89)).thenReturn(Optional.of(product));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            orderService.processDirectBuyCheckout(10, 1, 89, 1, "COD", "Direct buy", null)
        );

        assertTrue(ex.getMessage().contains("is currently unavailable"));
        verifyNoInteractions(cartService);
        verifyNoInteractions(cartDAO);
    }

    @Test
    @DisplayName("Should delegate filtered and sorted order retrieval to OrderDAO")
    void testGetUserOrdersFilteringAndSorting() {
        com.example.ecommerce.util.Pagination<Order> mockPage = new com.example.ecommerce.util.Pagination<>(java.util.Collections.emptyList(), 1, 10, 0);
        when(orderDAO.findByUserId(10, "SHIPPED", "price_high", 1, 10)).thenReturn(mockPage);

        com.example.ecommerce.util.Pagination<Order> result = orderService.getUserOrders(10, "SHIPPED", "price_high", 1, 10);

        assertNotNull(result);
        verify(orderDAO).findByUserId(10, "SHIPPED", "price_high", 1, 10);
    }
}
