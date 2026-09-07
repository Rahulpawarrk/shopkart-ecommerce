package com.example.ecommerce.order.repository;

import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserIdOrderByCreatedAtDesc(int userId);
    Page<Order> findByUserIdOrderByCreatedAtDesc(int userId, Pageable pageable);
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}
