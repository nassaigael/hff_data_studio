package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
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

	public AnalysisExecution toEntity(
			AnalysisExecutionRequest request,
			Dataset dataset,
			PredefinedAnalysis predefinedAnalysis,
			User user
	) {
		if (request == null || dataset == null || user == null) return null;
		return AnalysisExecution.builder()
				.status(AnalysisExecutionStatus.IN_PROGRESS)
				.usedParametersJson(request.getParameters().toString())
				.dataset(dataset)
				.predefinedAnalysis(predefinedAnalysis)
				.user(user)
				.build();
	}

	private String getStatusLabel(AnalysisExecutionStatus status) {
		if (status == null) return null;
		return switch (status) {
			case IN_PROGRESS -> "In progress";
			case ERROR -> "Error";
			case COMPLETED -> "Completed";
			case CANCELLED -> "Cancelled";
			case PENDING -> "Pending";
		};
	}

	private String formatDuration(Integer durationMs) {
		if (durationMs == null) return null;
		if (durationMs < 1000) return durationMs + "ms";
		if (durationMs < 60000) return String.format("%.2f s", durationMs / 1000.0);
		long minutes = durationMs / 60000;
		long seconds = (durationMs % 60000) / 1000;
		return String.format("%d min %d s", minutes, seconds);
	}

}
