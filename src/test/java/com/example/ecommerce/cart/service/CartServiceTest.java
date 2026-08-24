package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dao.CartDAO;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.dao.InventoryDAO;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests with Mockito")
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private InventoryDAO inventoryDAO;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("Should reject adding out-of-stock product to cart")
    void testAddToCartOutOfStock() {
        Product p = new Product();
        p.setProductId(1);
        p.setProductName("Laptop Pro");
        p.setStatus("ACTIVE");

        Inventory inv = new Inventory();
        inv.setProductId(1);
        inv.setQuantity(0); // Out of stock

        when(productDAO.findById(1)).thenReturn(Optional.of(p));
        when(inventoryDAO.findByProductId(1)).thenReturn(Optional.of(inv));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            cartService.addToCart(10, 1, 1)
        );

        assertTrue(ex.getMessage().contains("out of stock"));
    }

    @Test
    @DisplayName("Should reject adding quantity exceeding available inventory")
    void testAddToCartExceedsStock() {
        Product p = new Product();
        p.setProductId(2);
        p.setProductName("Wireless Earbuds");
        p.setStatus("ACTIVE");

        Inventory inv = new Inventory();
        inv.setProductId(2);
        inv.setQuantity(2); // 2 in stock

        when(productDAO.findById(2)).thenReturn(Optional.of(p));
        when(inventoryDAO.findByProductId(2)).thenReturn(Optional.of(inv));
        when(cartDAO.getCartWithItems(10)).thenReturn(Optional.of(new Cart(1, 10)));

        ValidationException ex = assertThrows(ValidationException.class, () ->
            cartService.addToCart(10, 2, 3) // Requesting 3 units (within max limit 3, but exceeds stock 2)
        );

        assertTrue(ex.getMessage().contains("Only 2 units available"));
    }

    @Test
    @DisplayName("Should reject adding quantity exceeding max purchase limit (3 units)")
    void testAddToCartExceedsPurchaseLimit() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
            cartService.addToCart(10, 2, 4) // Requesting 4 units
        );

        assertTrue(ex.getMessage().contains("Maximum allowed is 3 units"));
    }

    @Test
    @DisplayName("Should reject adding inactive product to cart")
    void testAddToCartInactiveProduct() {
        Product p = new Product();
        p.setProductId(3);
        p.setProductName("Discontinued Phone");
        p.setStatus("INACTIVE");

        when(productDAO.findById(3)).thenReturn(Optional.of(p));

        assertThrows(ValidationException.class, () ->
            cartService.addToCart(10, 3, 1)
        );
    }

    @Test
    @DisplayName("Should correctly calculate cart totals with discounts and taxes")
    void testCartCalculations() {
        Cart cart = new Cart(1, 10);

        CartItem item1 = new CartItem();
        item1.setProductId(1);
        item1.setUnitPrice(new BigDecimal("1000.00"));
        item1.setDiscountPercentage(new BigDecimal("10.00")); // 100 discount -> 900 each
        item1.setTaxPercentage(new BigDecimal("18.00"));      // 18% of 900 = 162 each
        item1.setQuantity(2);
        item1.setStockQuantity(10);

        CartItem item2 = new CartItem();
        item2.setProductId(2);
        item2.setUnitPrice(new BigDecimal("500.00"));
        item2.setDiscountPercentage(BigDecimal.ZERO);
        item2.setTaxPercentage(new BigDecimal("5.00"));       // 5% of 500 = 25
        item2.setQuantity(1);
        item2.setStockQuantity(10);

        cart.addItem(item1);
        cart.addItem(item2);

        // Subtotal = (1000 * 2) + (500 * 1) = 2500.00
        assertEquals(new BigDecimal("2500.00"), cart.getSubtotal());

        // Total Discount = (100 * 2) = 200.00
        assertEquals(new BigDecimal("200.00"), cart.getTotalDiscount());

        // Discounted Subtotal = 2500 - 200 = 2300.00
        assertEquals(new BigDecimal("2300.00"), cart.getDiscountedSubtotal());

        // Estimated Tax = (162 * 2) + (25 * 1) = 324 + 25 = 349.00
        assertEquals(new BigDecimal("349.00"), cart.getEstimatedTax());

        // Grand Total = 2300.00 + 349.00 = 2649.00
        assertEquals(new BigDecimal("2649.00"), cart.getGrandTotal());

        assertEquals(3, cart.getTotalQuantity());
        assertFalse(cart.hasUnavailableItems());
    }
}
