package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ApprovalStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {
	private UUID approvalId;
	private String entityType;
	private UUID entityId;
	private String title;
	private String comment;
	private ApprovalStatus status;
	private UUID requesterId;
	private String requesterFullName;
	private UUID reviewerId;
	private String reviewerFullName;
	private String reviewComment;
	private LocalDateTime reviewedAt;
	private LocalDateTime createdAt;
}