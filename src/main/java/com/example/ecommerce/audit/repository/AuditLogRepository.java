package com.example.ecommerce.audit.repository;

import com.example.ecommerce.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
