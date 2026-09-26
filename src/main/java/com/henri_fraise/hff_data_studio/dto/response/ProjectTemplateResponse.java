package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTemplateResponse {
	private UUID templateId;
	private String name;
	private String description;
	private String category;
	private Map<String, Object> config;
	private Boolean isPublic;
	private Long usageCount;
	private LocalDateTime createdAt;
}