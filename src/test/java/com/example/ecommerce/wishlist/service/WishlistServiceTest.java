package com.example.ecommerce.wishlist.service;

import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.product.dao.ProductDAO;
import com.example.ecommerce.product.model.Product;
import com.example.ecommerce.wishlist.dao.WishlistDAO;
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
@DisplayName("WishlistService Unit Tests with Mockito")
class WishlistServiceTest {

    @Mock
    private WishlistDAO wishlistDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private CartService cartService;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("Should reject adding inactive product to wishlist")
    void testAddToWishlistInactiveProduct() {
        Product inactive = new Product();
        inactive.setProductId(10);
        inactive.setStatus("INACTIVE");

        when(productDAO.findById(10)).thenReturn(Optional.of(inactive));

        assertThrows(ValidationException.class, () ->
            wishlistService.addToWishlist(1, 10)
        );

        verify(wishlistDAO, never()).addItem(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should move item from wishlist to shopping cart atomically")
    void testMoveToCartSuccess() {
        // Moving product 5 for user 1
        wishlistService.moveToCart(1, 5, 2);

        // Verify added to cart with quantity 2
        verify(cartService, times(1)).addToCart(1, 5, 2);

        // Verify removed from wishlist
        verify(wishlistDAO, times(1)).removeItemByUserId(1, 5);
    }
}
