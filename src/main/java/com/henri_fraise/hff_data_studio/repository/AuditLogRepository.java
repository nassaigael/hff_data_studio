package com.henri_fraise.hff_data_studio.repository;


import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

	Page<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId, Pageable pageable);

	Page<AuditLog> findByConcernedEntityOrderByActionDateDesc(String concernedEntity, Pageable pageable);

	Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

	List<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId);

}
