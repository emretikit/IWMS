package com.hacettepe.iwms.repository;

import com.hacettepe.iwms.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByUserId(Long userId);
}
