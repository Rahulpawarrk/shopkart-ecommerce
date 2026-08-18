package com.example.ecommerce.inventory.service;

import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.dao.InventoryDAO;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.inventory.model.InventoryTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests with Mockito")
class InventoryServiceTest {

    @Mock
    private InventoryDAO inventoryDAO;

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("Should successfully reserve stock and record SALE transaction when sufficient stock is available")
    void testReserveStockSuccess() throws SQLException {
        Inventory current = new Inventory();
        current.setProductId(1);
        current.setProductName("Laptop Pro");
        current.setQuantity(10);

        when(inventoryDAO.getInventoryWithLock(eq(1), any(Connection.class))).thenReturn(Optional.of(current));

        // Attempt to deduct 3 units for Order #1001
        inventoryService.reserveStockForOrder(1, 3, 1001, mockConnection);

        // Verify stock updated to 7 (10 - 3)
        verify(inventoryDAO, times(1)).updateStock(1, 7, mockConnection);
        verify(inventoryDAO, times(1)).recordTransaction(any(InventoryTransaction.class), eq(mockConnection));
    }

    @Test
    @DisplayName("Should throw ValidationException when requested quantity exceeds available stock")
    void testReserveStockInsufficientStock() throws SQLException {
        Inventory current = new Inventory();
        current.setProductId(2);
        current.setProductName("Wireless Headphones");
        current.setQuantity(2); // Only 2 in stock

        when(inventoryDAO.getInventoryWithLock(eq(2), any(Connection.class))).thenReturn(Optional.of(current));

        // Request 5 units (exceeds 2)
        ValidationException ex = assertThrows(ValidationException.class, () ->
            inventoryService.reserveStockForOrder(2, 5, 1002, mockConnection)
        );

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(inventoryDAO, never()).updateStock(anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Should reject negative quantity adjustments")
    void testAdjustStockNegativeQuantity() {
        assertThrows(ValidationException.class, () ->
            inventoryService.adjustStock(1, -5, "Damaged in warehouse", 1)
        );
    }

    @Test
    @DisplayName("Should reject restock with non-positive quantity")
    void testRestockZeroOrNegativeQuantity() {
        assertThrows(ValidationException.class, () ->
            inventoryService.restockProduct(1, 0, "SUPPLIER_RESTOCK", null, "Remarks", 1)
        );
        assertThrows(ValidationException.class, () ->
            inventoryService.restockProduct(1, -10, "SUPPLIER_RESTOCK", null, "Remarks", 1)
        );
    }
}
