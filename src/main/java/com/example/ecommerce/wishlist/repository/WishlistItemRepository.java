package com.example.ecommerce.wishlist.repository;

import com.example.ecommerce.wishlist.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Integer> {
    List<WishlistItem> findByWishlistId(int wishlistId);
    Optional<WishlistItem> findByWishlistIdAndProductId(int wishlistId, int productId);
    void deleteByWishlistIdAndProductId(int wishlistId, int productId);
}
