package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dao.CartDAO;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.inventory.dao.InventoryDAO;
import com.example.ecommerce.inventory.model.Inventory;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service Layer coordinating shopping cart operations, live stock validation,
 * and authoritative database pricing retrieval.
 */
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;
    private final InventoryDAO inventoryDAO;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.productDAO = new ProductDAO();
        this.inventoryDAO = new InventoryDAO();
    }

    public CartService(CartDAO cartDAO, ProductDAO productDAO, InventoryDAO inventoryDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
    }

    /**
     * Retrieves the customer's shopping cart enriched with live prices and stock.
     */
    public Cart getCart(int userId) {
        return cartDAO.getCartWithItems(userId)
                .orElseGet(() -> new Cart(0, userId));
    }

    /**
     * Adds an item to the shopping cart with live stock availability verification.
     */
    public void addToCart(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be at least 1.");
        }

        // 1. Verify Product is Active
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.isActive()) {
            throw new ValidationException("Product '" + product.getProductName() + "' is currently unavailable.");
        }

        // 2. Verify Stock Availability
        Inventory inventory = inventoryDAO.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not tracked for product: " + productId));

        if (inventory.getQuantity() <= 0) {
            throw new ValidationException("Product '" + product.getProductName() + "' is currently out of stock.");
        }

        // 3. Resolve Cart & Check Existing In-Cart Quantity
        Cart existingCart = getCart(userId);
        int currentInCartQty = 0;
        for (CartItem item : existingCart.getItems()) {
            if (item.getProductId() == productId) {
                currentInCartQty = item.getQuantity();
                break;
            }
        }

        int totalRequested = currentInCartQty + quantity;
        if (totalRequested > inventory.getQuantity()) {
            throw new ValidationException("Cannot add " + quantity + " units. Only " + inventory.getQuantity() + 
                    " units available in stock (You already have " + currentInCartQty + " in your cart).");
        }

        // 4. Upsert Item into Cart
        try (Connection conn = DBConnection.getConnection()) {
            int cartId = cartDAO.getOrCreateCartId(userId, conn);
            cartDAO.upsertCartItem(cartId, productId, quantity);
            logger.info("Added {} units of product [{}] to user [{}] cart.", quantity, productId, userId);
        } catch (SQLException e) {
            logger.error("Error adding product to cart for userId: {}", userId, e);
            throw new DatabaseException("Failed to add product to cart", e);
        }
    }

    /**
     * Updates exact quantity of an item in the cart.
     */
    public void updateQuantity(int userId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeFromCart(userId, productId);
            return;
        }

        Inventory inventory = inventoryDAO.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        if (newQuantity > inventory.getQuantity()) {
            throw new ValidationException("Requested quantity (" + newQuantity + 
                    ") exceeds available stock (" + inventory.getQuantity() + ").");
        }

        try (Connection conn = DBConnection.getConnection()) {
            int cartId = cartDAO.getOrCreateCartId(userId, conn);
            cartDAO.updateItemQuantity(cartId, productId, newQuantity);
            logger.info("Updated quantity for product [{}] in user [{}] cart to {}.", productId, userId, newQuantity);
        } catch (SQLException e) {
            logger.error("Error updating cart item quantity", e);
            throw new DatabaseException("Failed to update cart quantity", e);
        }
    }

    /**
     * Removes an item from the customer's cart.
     */
    public void removeFromCart(int userId, int productId) {
        try (Connection conn = DBConnection.getConnection()) {
            int cartId = cartDAO.getOrCreateCartId(userId, conn);
            cartDAO.removeItem(cartId, productId);
            logger.info("Removed product [{}] from user [{}] cart.", productId, userId);
        } catch (SQLException e) {
            logger.error("Error removing product from cart", e);
            throw new DatabaseException("Failed to remove item from cart", e);
        }
    }

    /**
     * Clears all items in the customer's cart.
     */
    public void clearCart(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            int cartId = cartDAO.getOrCreateCartId(userId, conn);
            cartDAO.clearCart(cartId, conn);
            logger.info("Cleared shopping cart for user [{}]", userId);
        } catch (SQLException e) {
            logger.error("Error clearing cart for userId: {}", userId, e);
            throw new DatabaseException("Failed to clear shopping cart", e);
        }
    }

    /**
     * Returns total item count in user's cart for header counters.
     */
    public int getCartItemCount(int userId) {
        return cartDAO.getItemCount(userId);
    }
}
