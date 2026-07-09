package com.paisanotes.repository;

import com.paisanotes.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

	List<AuditLog> findByUserIdAndCreatedAtGreaterThan(UUID userId, ZonedDateTime lastSync);
}
