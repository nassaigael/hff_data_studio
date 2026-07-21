package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.AnalysisExecutionMapper;
import com.henri_fraise.hff_data_studio.repository.AnalysisExecutionRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisExecutionService {

	private final AnalysisExecutionRepository executionRepository;
	private final AnalysisExecutionMapper executionMapper;
	private final DatasetService datasetService;
	private final PredefinedAnalysisService predefinedAnalysisService;
	private final UserService userService;
	private final SecurityUtils securityUtils;
	private final PythonAnalysisService pythonAnalysisService;

	@Transactional
	public AnalysisExecution createExecution(AnalysisExecutionRequest request) {
		Dataset dataset = datasetService.getDatasetEntityById(request.getDatasetId());
		User user = userService.getUserEntityById(securityUtils.getCurrentUserId());

		PredefinedAnalysis analysis = null;
		if (request.getAnalysisId() != null) {
			analysis = predefinedAnalysisService.getAnalysisEntityById(request.getAnalysisId());
		}

		AnalysisExecution execution = executionMapper.toEntity(request, dataset, analysis, user);
		AnalysisExecution saved = executionRepository.save(execution);
		log.info("Analysis execution created: {} for dataset: {}", saved.getId(), dataset.getDatasetName());
		return saved;
	}

	@Async
	@Transactional
	public CompletableFuture<AnalysisExecution> executeAnalysisAsync(UUID executionId) {
		try {
			AnalysisExecution execution = getExecutionEntityById(executionId);
			execution.setStatus(AnalysisExecutionStatus.IN_PROGRESS);
			executionRepository.save(execution);

			pythonAnalysisService.executeAnalysis(executionId);

			return CompletableFuture.completedFuture(execution);
		} catch (Exception e) {
			log.error("Error executing analysis asynchronously: {}", e.getMessage(), e);
			updateExecutionStatus(executionId, AnalysisExecutionStatus.ERROR, e.getMessage());
			return CompletableFuture.failedFuture(e);
		}
	}

	@Transactional
	public AnalysisExecution executeAnalysisSync(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		long startTime = System.currentTimeMillis();

		try {
			execution.setStatus(AnalysisExecutionStatus.IN_PROGRESS);
			executionRepository.save(execution);

			pythonAnalysisService.executeAnalysis(executionId);

			long duration = System.currentTimeMillis() - startTime;
			execution.setDurationMs((int) duration);
			execution.setStatus(AnalysisExecutionStatus.COMPLETED);
			executionRepository.save(execution);

			log.info("Analysis execution completed: {} in {}ms", executionId, duration);
			return execution;

		} catch (Exception e) {
			log.error("Error executing analysis: {}", e.getMessage(), e);
			long duration = System.currentTimeMillis() - startTime;
			execution.setDurationMs((int) duration);
			execution.setStatus(AnalysisExecutionStatus.ERROR);
			executionRepository.save(execution);
			throw new RuntimeException("Failed to execute analysis: " + e.getMessage());
		}
	}

	public AnalysisExecution getExecutionEntityById(UUID executionId) {
		return executionRepository.findById(executionId)
				.orElseThrow(() -> new ResourceNotFoundException("Analysis execution not found: " + executionId));
	}

	public AnalysisExecutionResponse getExecutionById(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		return executionMapper.toResponse(execution);
	}

	public Page<AnalysisExecutionResponse> getExecutions(UUID datasetId, Pageable pageable) {
		Page<AnalysisExecution> executions;
		if (datasetId != null) {
			executions = executionRepository.findByDatasetIdOrderByExecutedAtDesc(datasetId, pageable);
		} else {
			executions = executionRepository.findAll(pageable);
		}
		return executions.map(executionMapper::toResponse);
	}

	public List<AnalysisExecution> getExecutionsByDataset(UUID datasetId) {
		return executionRepository.findByDatasetIdOrderByExecutedAtDesc(datasetId);
	}

	public List<AnalysisExecution> getExecutionsByUser(UUID userId) {
		return executionRepository.findByUserIdOrderByExecutedAtDesc(userId);
	}

	public List<AnalysisExecution> getExecutionsByStatus(AnalysisExecutionStatus status) {
		return executionRepository.findByStatus(status);
	}

	public Page<AnalysisExecution> getExecutionsByStatus(AnalysisExecutionStatus status, Pageable pageable) {
		return executionRepository.findByStatus(status, pageable);
	}

	public List<AnalysisExecution> getExecutionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		return executionRepository.findByExecutedAtBetween(startDate, endDate);
	}

	public List<AnalysisExecution> getRecentExecutions(int limit) {
		return executionRepository.findRecentExecutions(Pageable.ofSize(limit));
	}

	public List<AnalysisExecution> getFailedExecutions() {
		return executionRepository.findByStatus(AnalysisExecutionStatus.ERROR);
	}

	@Transactional
	public void updateExecutionStatus(UUID executionId, AnalysisExecutionStatus status, Integer durationMs) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		execution.setStatus(status);
		if (durationMs != null) {
			execution.setDurationMs(durationMs);
		}
		executionRepository.save(execution);
		log.info("Execution status updated: {} -> {}", executionId, status);
	}

	@Transactional
	public void updateExecutionStatus(UUID executionId, AnalysisExecutionStatus status, String errorMessage) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		execution.setStatus(status);
		executionRepository.save(execution);
		log.error("Execution failed: {} - {}", executionId, errorMessage);
	}

	@Transactional
	public void updateExecutionWithResults(UUID executionId, AnalysisExecutionStatus status,
	                                       Integer durationMs, String usedParametersJson) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		execution.setStatus(status);
		if (durationMs != null) {
			execution.setDurationMs(durationMs);
		}
		if (usedParametersJson != null) {
			execution.setUsedParametersJson(usedParametersJson);
		}
		executionRepository.save(execution);
		log.info("Execution updated with results: {}", executionId);
	}

	@Transactional
	public void cancelExecution(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		if (execution.getStatus() == AnalysisExecutionStatus.IN_PROGRESS ||
				execution.getStatus() == AnalysisExecutionStatus.PENDING) {
			execution.setStatus(AnalysisExecutionStatus.CANCELLED);
			executionRepository.save(execution);
			pythonAnalysisService.cancelExecution(executionId);
			log.info("Execution cancelled: {}", executionId);
		} else {
			throw new IllegalStateException("Cannot cancel execution with status: " + execution.getStatus());
		}
	}

	@Transactional
	public void deleteExecution(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		if (execution.getStatus() == AnalysisExecutionStatus.IN_PROGRESS) {
			throw new IllegalStateException("Cannot delete execution while it is in progress");
		}
		executionRepository.delete(execution);
		log.info("Execution deleted: {}", executionId);
	}

	@Transactional
	public void deleteExecutionsByDataset(UUID datasetId) {
		List<AnalysisExecution> executions = getExecutionsByDataset(datasetId);
		int deletedCount = 0;
		for (AnalysisExecution execution : executions) {
			if (execution.getStatus() != AnalysisExecutionStatus.IN_PROGRESS) {
				executionRepository.delete(execution);
				deletedCount++;
			}
		}
		log.info("Deleted {} executions for dataset: {}", deletedCount, datasetId);
	}

	public long countExecutionsByDataset(UUID datasetId) {
		return executionRepository.countByDatasetId(datasetId);
	}

	public long countExecutionsByUser(UUID userId) {
		return executionRepository.countByUserId(userId);
	}

	public long countExecutionsByStatus(AnalysisExecutionStatus status) {
		return executionRepository.countByStatus(status);
	}

	public long countAllExecutions() {
		return executionRepository.count();
	}

	public long countExecutionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return executionRepository.countByExecutedAtBetween(startDate, endDate);
	}

	public Double getAverageExecutionTime() {
		return executionRepository.averageDuration();
	}

	public Double getAverageExecutionTimeByAnalysis(UUID analysisId) {
		return executionRepository.averageDurationByAnalysisId(analysisId);
	}

	public Double getAverageExecutionTimeByUser(UUID userId) {
		return executionRepository.averageDurationByUserId(userId);
	}

	public Double getAverageExecutionTimeByDataset(UUID datasetId) {
		return executionRepository.averageDurationByDatasetId(datasetId);
	}

	public long countExecutionsByAnalysis(UUID analysisId) {
		return executionRepository.countByPredefinedAnalysisId(analysisId);
	}

	public List<Object[]> countByStatusGrouped() {
		return executionRepository.countGroupByStatus();
	}

	public List<Object[]> countByUserGrouped() {
		return executionRepository.countGroupByUser();
	}

	public List<Object[]> countByDatasetGrouped() {
		return executionRepository.countGroupByDataset();
	}

	public Map<String, Object> getExecutionStatistics(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		Map<String, Object> stats = new HashMap<>();
		stats.put("executionId", execution.getId());
		stats.put("status", execution.getStatus());
		stats.put("durationMs", execution.getDurationMs());
		stats.put("executedAt", execution.getExecutedAt());
		stats.put("datasetId", execution.getDataset() != null ? execution.getDataset().getId() : null);
		stats.put("datasetName", execution.getDataset() != null ? execution.getDataset().getDatasetName() : null);
		stats.put("analysisId", execution.getPredefinedAnalysis() != null ? execution.getPredefinedAnalysis().getId() : null);
		stats.put("analysisName", execution.getPredefinedAnalysis() != null ? execution.getPredefinedAnalysis().getAnalysisName() : "Custom Analysis");
		stats.put("userId", execution.getUser() != null ? execution.getUser().getId() : null);
		stats.put("userFullName", execution.getUser() != null
				? execution.getUser().getFirstName() + " " + execution.getUser().getLastName()
				: null);
		stats.put("resultCount", execution.getResults() != null ? execution.getResults().size() : 0);
		return stats;
	}

	public Map<String, Object> getGlobalStatistics() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalExecutions", countAllExecutions());
		stats.put("totalCompleted", countExecutionsByStatus(AnalysisExecutionStatus.COMPLETED));
		stats.put("totalInProgress", countExecutionsByStatus(AnalysisExecutionStatus.IN_PROGRESS));
		stats.put("totalError", countExecutionsByStatus(AnalysisExecutionStatus.ERROR));
		stats.put("totalCancelled", countExecutionsByStatus(AnalysisExecutionStatus.CANCELLED));
		stats.put("totalPending", countExecutionsByStatus(AnalysisExecutionStatus.PENDING));
		stats.put("averageExecutionTimeMs", getAverageExecutionTime());
		stats.put("averageExecutionTimeFormatted", formatDuration(getAverageExecutionTime()));
		return stats;
	}

	private String formatDuration(Double durationMs) {
		if (durationMs == null) {
			return "N/A";
		}
		if (durationMs < 1000) {
			return String.format("%.0f ms", durationMs);
		}
		if (durationMs < 60000) {
			return String.format("%.2f s", durationMs / 1000);
		}
		long minutes = (long) (durationMs / 60000);
		long seconds = (long) ((durationMs % 60000) / 1000);
		return String.format("%d min %d s", minutes, seconds);
	}

	public boolean isExecutionInProgress(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		return execution.getStatus() == AnalysisExecutionStatus.IN_PROGRESS ||
				execution.getStatus() == AnalysisExecutionStatus.PENDING;
	}

	public boolean isExecutionCompleted(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		return execution.getStatus() == AnalysisExecutionStatus.COMPLETED;
	}

	public boolean isExecutionFailed(UUID executionId) {
		AnalysisExecution execution = getExecutionEntityById(executionId);
		return execution.getStatus() == AnalysisExecutionStatus.ERROR;
	}
}