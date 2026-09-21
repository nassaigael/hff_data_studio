package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
	private UUID tenantId;
	private String code;
	private String name;
	private String domain;
	private Boolean isActive;
	private Integer maxUsers;
	private Long maxStorageMb;
	private LocalDateTime createdAt;
}