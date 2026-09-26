package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ApprovalStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "approval_request")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApprovalRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "approval_id")
	private UUID id;

	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private ApprovalStatus status = ApprovalStatus.PENDING;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private User reviewer;

	@Column(name = "review_comment", columnDefinition = "TEXT")
	private String reviewComment;

	@Column(name = "reviewed_at")
	private LocalDateTime reviewedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}