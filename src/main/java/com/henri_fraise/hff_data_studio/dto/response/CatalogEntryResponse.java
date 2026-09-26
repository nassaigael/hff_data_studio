package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEntryResponse {
	private UUID entryId;
	private String entityType;
	private UUID entityId;
	private String displayName;
	private String description;
	private String businessDomain;
	private String ownerName;
	private String stewardName;
	private String classification;
	private String sensitivityLevel;
	private List<String> tags;
	private Map<String, Object> metadata;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}