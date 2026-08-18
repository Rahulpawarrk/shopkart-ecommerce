package com.example.ecommerce.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable audit ledger entry representing a physical movement of inventory.
 */
public class InventoryTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private int transactionId;
    private int productId;
    private String productName;
    private String sku;
    private int previousStock;
    private int quantityChanged;
    private int newStock;
    private TransactionType transactionType;
    private String referenceType;
    private Integer referenceId;
    private String remarks;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    public InventoryTransaction() {}

    public InventoryTransaction(int productId, int previousStock, int quantityChanged, int newStock, 
                                TransactionType transactionType, String referenceType, Integer referenceId, 
                                String remarks, Integer createdBy) {
        this.productId = productId;
        this.previousStock = previousStock;
        this.quantityChanged = quantityChanged;
        this.newStock = newStock;
        this.transactionType = transactionType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.remarks = remarks;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(int previousStock) {
        this.previousStock = previousStock;
    }

    public int getQuantityChanged() {
        return quantityChanged;
    }

    public void setQuantityChanged(int quantityChanged) {
        this.quantityChanged = quantityChanged;
    }

    public int getNewStock() {
        return newStock;
    }

    public void setNewStock(int newStock) {
        this.newStock = newStock;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
