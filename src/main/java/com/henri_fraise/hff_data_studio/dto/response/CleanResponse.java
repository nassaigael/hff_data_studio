package com.henri_fraise.hff_data_studio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CleanResponse(
		@JsonProperty("output_path") String outputPath,
		@JsonProperty("total_rules") int totalRules,
		@JsonProperty("success_count") int successCount,
		@JsonProperty("failure_count") int failureCount,
		@JsonProperty("affected_rows") int affectedRows,
		@JsonProperty("row_count") int rowCount,
		@JsonProperty("column_count") int columnCount,
		@JsonProperty("duration_ms") long durationMs,
		@JsonProperty("rule_results") List<RuleResult> ruleResults,
		@JsonProperty("column_stats") List<ColumnStat> columnStats) {

	public record RuleResult(
			String column,
			String type,
			String status,
			@JsonProperty("affected_rows") int affectedRows,
			@JsonProperty("duration_ms") long durationMs,
			String error) {}

	public record ColumnStat(
			String name,
			@JsonProperty("null_count") int nullCount,
			@JsonProperty("unique_count") int uniqueCount) {}
}