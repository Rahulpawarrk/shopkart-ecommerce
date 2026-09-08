package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.*;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.coupon.model.Coupon;
import com.example.ecommerce.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Customer Shopping Cart management and Promotional Coupon application.
 */
@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;
    private final CouponService couponService;

    @Autowired
    public CartRestController(CartService cartService, CouponService couponService) {
        this.cartService = cartService;
        this.couponService = couponService;
    }

    private UserSession getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
    }

    private Coupon getSessionCoupon(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Coupon) session.getAttribute("appliedCoupon") : null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(HttpServletRequest request) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to view your cart", "UNAUTHORIZED"));
        }

        Cart cart = cartService.getCart(user.getUserId());
        Coupon coupon = getSessionCoupon(request);

        // Update session
        HttpSession session = request.getSession(true);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(ApiResponse.ok(CartDto.fromEntity(cart, coupon)));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @Valid @RequestBody AddToCartRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to add items to your cart", "UNAUTHORIZED"));
        }

        cartService.addToCart(user.getUserId(), req.getProductId(), req.getQuantity());

        Cart cart = cartService.getCart(user.getUserId());
        Coupon coupon = getSessionCoupon(request);

        HttpSession session = request.getSession(true);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", CartDto.fromEntity(cart, coupon)));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @PathVariable int cartItemId,
            @Valid @RequestBody UpdateCartItemRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to update cart items", "UNAUTHORIZED"));
        }

        cartService.updateQuantity(user.getUserId(), cartItemId, req.getQuantity());

        Cart cart = cartService.getCart(user.getUserId());
        Coupon coupon = getSessionCoupon(request);

        HttpSession session = request.getSession(true);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(ApiResponse.ok("Cart updated", CartDto.fromEntity(cart, coupon)));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDto>> removeCartItem(
            @PathVariable int cartItemId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to remove cart items", "UNAUTHORIZED"));
        }

        cartService.removeFromCart(user.getUserId(), cartItemId);

        Cart cart = cartService.getCart(user.getUserId());
        Coupon coupon = getSessionCoupon(request);

        HttpSession session = request.getSession(true);
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", CartDto.fromEntity(cart, coupon)));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartDto>> clearCart(HttpServletRequest request) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to clear your cart", "UNAUTHORIZED"));
        }

        cartService.clearCart(user.getUserId());

        Cart cart = cartService.getCart(user.getUserId());
        HttpSession session = request.getSession(true);
        session.removeAttribute("appliedCoupon");
        session.setAttribute("cart", cart);

        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", CartDto.fromEntity(cart, null)));
    }

    @PostMapping("/coupon")
    public ResponseEntity<ApiResponse<CartDto>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to apply coupons", "UNAUTHORIZED"));
        }

        Cart cart = cartService.getCart(user.getUserId());
        if (cart.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cannot apply coupon to an empty cart", "EMPTY_CART"));
        }

        Coupon coupon = couponService.validateAndApplyCoupon(req.getCode().trim().toUpperCase(), cart.getGrandTotal());

        HttpSession session = request.getSession(true);
        session.setAttribute("appliedCoupon", coupon);

        return ResponseEntity.ok(ApiResponse.ok("Coupon applied: " + coupon.getCode(), CartDto.fromEntity(cart, coupon)));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<ApiResponse<CartDto>> removeCoupon(HttpServletRequest request) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("appliedCoupon");
        }

        Cart cart = cartService.getCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Coupon removed", CartDto.fromEntity(cart, null)));
    }
}
