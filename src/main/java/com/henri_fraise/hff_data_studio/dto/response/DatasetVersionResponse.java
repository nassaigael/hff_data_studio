package com.henri_fraise.hff_data_studio.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetVersionResponse {
	private UUID versionId;
	private UUID datasetId;
	private Integer versionNumber;
	private String label;
	private Integer rowCount;
	private Integer columnCount;
	private Double qualityScore;
	private Boolean isCurrent;
	private LocalDateTime createdAt;
}