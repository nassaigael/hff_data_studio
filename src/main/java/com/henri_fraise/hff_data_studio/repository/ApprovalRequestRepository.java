package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ApprovalRequest;
import com.henri_fraise.hff_data_studio.enums.ApprovalStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

	Page<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(UUID requesterId, Pageable pageable);

	Page<ApprovalRequest> findByStatusOrderByCreatedAtDesc(ApprovalStatus status, Pageable pageable);

	List<ApprovalRequest> findByEntityTypeAndEntityId(String entityType, UUID entityId);

	long countByStatus(ApprovalStatus status);
}