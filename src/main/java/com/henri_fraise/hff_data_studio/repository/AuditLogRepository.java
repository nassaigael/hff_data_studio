package com.henri_fraise.hff_data_studio.repository;


import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

	Page<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId, Pageable pageable);

	Page<AuditLog> findByConcernedEntityOrderByActionDateDesc(String concernedEntity, Pageable pageable);

	Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

	List<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId);

	long countByUserId(UUID userId);

	long countByConcernedEntity(String concernedEntity);

	long countByActionContainingIgnoreCase(String action);

	@Query("SELECT a FROM AuditLog a WHERE a.actionDate BETWEEN  :start_date AND :end_date ORDER BY a.actionDate DESC ")
	Page<AuditLog> findByActionDateBetween(LocalDateTime start_date, LocalDateTime end_date, Pageable pageable);

	@Query("SELECT a FROM AuditLog a WHERE a.user.id = :user_id AND a.actionDate BETWEEN :start_date AND :end_date")
	List<AuditLog> findByUserIdAndDateRange(UUID user_id, LocalDateTime start_date, LocalDateTime end_date);

	@Query("SELECT a FROM AuditLog a WHERE a.concernedEntity = :entity AND a.entityId = :entity_id")
	List<AuditLog> findByConcernedEntityAndEntityId(String entity, UUID entity_id);

	@Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC ")
	List<Object[]> countActions();

	@Query("SELECT COUNT(a) FROM AuditLog a WHERE a.actionDate BETWEEN :start_date AND :end_date")
	long countLogsBetween(LocalDateTime start_date, LocalDateTime end_date);

	@Query("SELECT a FROM AuditLog a WHERE a.actionDate < :date")
	List<AuditLog> findOldAuditLogs(LocalDateTime date);
	
}
