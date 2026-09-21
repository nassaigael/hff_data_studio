package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDiffResponse {
	private UUID datasetId;
	private Integer fromVersion;
	private Integer toVersion;
	private int rowCountDelta;
	private int columnCountDelta;
	private double qualityScoreDelta;
	private List<String> addedColumns;
	private List<String> removedColumns;
	private boolean schemaChanged;
	private boolean dataChanged;
}