package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalysisExecutionMapper {

	private final AnalysisResultMapper resultMapper;

	public AnalysisExecutionResponse toResponse(AnalysisExecution execution) {
		if (execution == null) return null;

		return AnalysisExecutionResponse.builder()
				.executionId(execution.getId())
				.executedAt(execution.getExecutedAt())
				.status(execution.getStatus())
				.statusLabel(getStatusLabel(execution.getStatus()))
				.durationMs(execution.getDurationMs())
				.durationFormatted(formatDuration(execution.getDurationMs()))
				.usedParametersJson(execution.getUsedParametersJson())
				.datasetId(execution.getDataset() != null ? execution.getDataset().getId() : null)
				.datasetName(execution.getDataset() != null ? execution.getDataset().getDatasetName() : null)
				.analysisId(
						execution.getPredefinedAnalysis() != null
								? execution.getPredefinedAnalysis().getId()
								: null
				)
				.analysisName(
						execution.getPredefinedAnalysis() != null
								? execution.getPredefinedAnalysis().getAnalysisName()
								: "Custom name"
				)
				.analysisCategory(
						execution.getPredefinedAnalysis() != null
								? execution.getPredefinedAnalysis().getCategory()
								: null
				)
				.userId(execution.getUser() != null ? execution.getUser().getId() : null)
				.userFullName(
						execution.getUser() != null
								? execution.getUser().getFirstName() + " " + execution.getUser().getLastName()
								: null
				)
				.results(
						execution.getResults() != null
								? execution.getResults()
								.stream()
								.map(resultMapper::toResponse)
								.collect(Collectors.toList())
								: null
				)
				.resultCount(execution.getResults() != null ? execution.getResults().size() : 0)
				.build();
	}

}
