package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DryRunResponse {
	private UUID datasetId;
	private int totalRules;
	private int affectedRows;
	private int affectedColumns;
	private long estimatedDurationMs;
	private List<RulePreview> previews;
	private boolean canProceed;
	private List<String> warnings;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RulePreview {
		private UUID ruleId;
		private String ruleType;
		private String columnName;
		private int estimatedAffectedRows;
		private int sampleBefore;
		private int sampleAfter;
		private Map<String, Object> sampleChanges;
		private String warning;
	}
}