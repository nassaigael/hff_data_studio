package com.henri_fraise.hff_data_studio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record ColumnProfileResponse(
		@JsonProperty("column_name") String columnName,
		@JsonProperty("detected_type") String detectedType,
		@JsonProperty("health_score") Double healthScore,
		List<SuggestionDto> suggestions,
		List<ExceptionAlertDto> exceptions) {

	public record SuggestionDto(
			@JsonProperty("rule_type") String ruleType,
			Double confidence,
			String severity,
			String reason,
			@JsonProperty("affected_rows") Integer affectedRows,
			Map<String, Object> parameters,
			List<Map<String, Object>> preview) {}

	public record ExceptionAlertDto(
			String severity, String message, @JsonProperty("row_indices") List<Integer> rowIndices) {}
}