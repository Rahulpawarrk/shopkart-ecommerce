package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.ApiResponse;
import com.example.ecommerce.api.dto.WishlistDto;
import com.example.ecommerce.api.dto.WishlistItemDto;
import com.example.ecommerce.api.dto.WishlistToggleRequest;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.wishlist.model.Wishlist;
import com.example.ecommerce.wishlist.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for customer wishlist management.
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistRestController {

    private final WishlistService wishlistService;

    @Autowired
    public WishlistRestController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    private UserSession getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistDto>> getWishlist(HttpServletRequest request) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to view your wishlist", "UNAUTHORIZED"));
        }

        Wishlist wishlist = wishlistService.getWishlist(user.getUserId());
        WishlistDto dto = new WishlistDto();
        dto.setWishlistId(wishlist.getWishlistId());
        dto.setUserId(wishlist.getUserId());
        dto.setTotalItems(wishlist.getItems() != null ? wishlist.getItems().size() : 0);
        if (wishlist.getItems() != null) {
            dto.setItems(wishlist.getItems().stream().map(WishlistItemDto::fromEntity).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(
            @Valid @RequestBody WishlistToggleRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to manage your wishlist", "UNAUTHORIZED"));
        }

        boolean inWishlist = wishlistService.isInWishlist(user.getUserId(), req.getProductId());
        if (inWishlist) {
            wishlistService.removeFromWishlist(user.getUserId(), req.getProductId());
            return ResponseEntity.ok(ApiResponse.ok("Removed from wishlist", Map.of(
                    "inWishlist", false,
                    "productId", req.getProductId()
            )));
        } else {
            wishlistService.addToWishlist(user.getUserId(), req.getProductId());
            return ResponseEntity.ok(ApiResponse.ok("Added to wishlist", Map.of(
                    "inWishlist", true,
                    "productId", req.getProductId()
            )));
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable int productId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to manage your wishlist", "UNAUTHORIZED"));
        }

        wishlistService.removeFromWishlist(user.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from wishlist", null));
    }
}
