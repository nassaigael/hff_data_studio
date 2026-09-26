package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogEntryRequest {

	@NotBlank private String entityType;
	@NotNull private UUID entityId;
	@NotBlank private String displayName;
	private String description;
	private String businessDomain;
	private String ownerName;
	private String stewardName;
	private String classification;
	private String sensitivityLevel;
	private List<String> tags;
	private Map<String, Object> metadata;
}