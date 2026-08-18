package com.example.ecommerce.inventory.model;

/**
 * Enumeration of all supported inventory transaction movement types.
 */
public enum TransactionType {
    PURCHASE,      // Supplier restock or opening inventory
    SALE,          // Customer order placement
    RETURN,        // Customer return restocked
    CANCELLATION,  // Order cancellation restored
    ADJUSTMENT     // Manual audit count correction
}
