package com.example.ecommerce.wishlist.service;

import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.config.DBConnection;
import com.example.ecommerce.exception.DatabaseException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.wishlist.dao.WishlistDAO;
import com.example.ecommerce.wishlist.model.Wishlist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Layer managing customer Wishlists and Move-to-Cart transfers.
 */
@Service
@Transactional
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;
    private final CartService cartService;

    public WishlistService() {
        this.wishlistDAO = new WishlistDAO();
        this.productDAO = new ProductDAO();
        this.cartService = new CartService();
    }

    public WishlistService(WishlistDAO wishlistDAO, ProductDAO productDAO, CartService cartService) {
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
        this.cartService = cartService;
    }

    public Wishlist getWishlist(int userId) {
        return wishlistDAO.getWishlistWithItems(userId)
                .orElseGet(() -> new Wishlist(0, userId));
    }

    public void addToWishlist(int userId, int productId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (!product.isActive()) {
            throw new ValidationException("Product is unavailable and cannot be saved to wishlist.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            int wishlistId = wishlistDAO.getOrCreateWishlistId(userId, conn);
            wishlistDAO.addItem(wishlistId, productId);
            logger.info("Added product [{}] to user [{}] wishlist.", productId, userId);
        } catch (SQLException e) {
            logger.error("Error adding product to wishlist for userId: {}", userId, e);
            throw new DatabaseException("Failed to add item to wishlist", e);
        }
    }

    public void removeFromWishlist(int userId, int productId) {
        wishlistDAO.removeItemByUserId(userId, productId);
        logger.info("Removed product [{}] from user [{}] wishlist.", productId, userId);
    }

    /**
     * Atomically transfers a saved product from Wishlist into the customer's Shopping Cart.
     */
    public void moveToCart(int userId, int productId, int quantity) {
        // Add to Cart first (validates stock availability)
        cartService.addToCart(userId, productId, quantity);

        // Remove from Wishlist
        removeFromWishlist(userId, productId);
        logger.info("Moved product [{}] from wishlist to cart for userId [{}]", productId, userId);
    }

    public boolean isInWishlist(int userId, int productId) {
        return wishlistDAO.isInWishlist(userId, productId);
    }

    public int getWishlistItemCount(int userId) {
        return wishlistDAO.getItemCount(userId);
    }
}
