package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuditLogResponse {
	private UUID logId;
	private String action;
	private String concernedEntity;
	private UUID entityId;
	private LocalDateTime actionDate;
	private String ipAddress;
	private UUID userId;
	private String userFullName;
	private String userEmail;
}
