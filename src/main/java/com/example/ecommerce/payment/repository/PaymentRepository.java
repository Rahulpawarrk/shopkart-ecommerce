package com.example.ecommerce.payment.repository;

import com.example.ecommerce.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findByOrderId(int orderId);
}
