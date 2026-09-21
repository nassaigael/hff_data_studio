package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.ApprovalRequestDto;
import com.henri_fraise.hff_data_studio.dto.response.ApprovalResponse;
import com.henri_fraise.hff_data_studio.entity.ApprovalRequest;
import com.henri_fraise.hff_data_studio.enums.ApprovalStatus;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.ApprovalRequestRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

	private final ApprovalRequestRepository approvalRepository;
	private final NotificationService notificationService;
	private final SecurityUtils securityUtils;

	@Transactional
	public ApprovalResponse create(ApprovalRequestDto request) {
		ApprovalRequest approval = ApprovalRequest.builder()
				.entityType(request.getEntityType())
				.entityId(request.getEntityId())
				.title(request.getTitle())
				.comment(request.getComment())
				.requester(securityUtils.getCurrentUser())
				.build();
		return toResponse(approvalRepository.save(approval));
	}

	@Transactional
	public ApprovalResponse approve(UUID approvalId, String comment) {
		ApprovalRequest approval = getEntity(approvalId);
		approval.setStatus(ApprovalStatus.APPROVED);
		approval.setReviewer(securityUtils.getCurrentUser());
		approval.setReviewComment(comment);
		approval.setReviewedAt(LocalDateTime.now());
		approvalRepository.save(approval);

		notificationService.send(
				approval.getRequester().getId(),
				NotificationType.SUCCESS,
				"Request approved",
				"Your request '" + approval.getTitle() + "' was approved",
				"/approvals/" + approvalId);

		return toResponse(approval);
	}

	@Transactional
	public ApprovalResponse reject(UUID approvalId, String comment) {
		ApprovalRequest approval = getEntity(approvalId);
		approval.setStatus(ApprovalStatus.REJECTED);
		approval.setReviewer(securityUtils.getCurrentUser());
		approval.setReviewComment(comment);
		approval.setReviewedAt(LocalDateTime.now());
		approvalRepository.save(approval);

		notificationService.send(
				approval.getRequester().getId(),
				NotificationType.WARNING,
				"Request rejected",
				"Your request '" + approval.getTitle() + "' was rejected",
				"/approvals/" + approvalId);

		return toResponse(approval);
	}

	@Transactional(readOnly = true)
	public Page<ApprovalResponse> listMine(Pageable pageable) {
		return approvalRepository.findByRequesterIdOrderByCreatedAtDesc(
				securityUtils.getCurrentUserId(), pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<ApprovalResponse> listPending(Pageable pageable) {
		return approvalRepository.findByStatusOrderByCreatedAtDesc(
				ApprovalStatus.PENDING, pageable).map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public List<ApprovalResponse> listForEntity(String entityType, UUID entityId) {
		return approvalRepository.findByEntityTypeAndEntityId(entityType, entityId)
				.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public boolean isApproved(String entityType, UUID entityId) {
		return approvalRepository.findByEntityTypeAndEntityId(entityType, entityId)
				.stream().anyMatch(a -> a.getStatus() == ApprovalStatus.APPROVED);
	}

	private ApprovalRequest getEntity(UUID id) {
		return approvalRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));
	}

	private ApprovalResponse toResponse(ApprovalRequest a) {
		return ApprovalResponse.builder()
				.approvalId(a.getId())
				.entityType(a.getEntityType())
				.entityId(a.getEntityId())
				.title(a.getTitle())
				.comment(a.getComment())
				.status(a.getStatus())
				.requesterId(a.getRequester() != null ? a.getRequester().getId() : null)
				.requesterFullName(a.getRequester() != null ? a.getRequester().getFullName() : null)
				.reviewerId(a.getReviewer() != null ? a.getReviewer().getId() : null)
				.reviewerFullName(a.getReviewer() != null ? a.getReviewer().getFullName() : null)
				.reviewComment(a.getReviewComment())
				.reviewedAt(a.getReviewedAt())
				.createdAt(a.getCreatedAt())
				.build();
	}
}