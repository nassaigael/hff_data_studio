package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import com.henri_fraise.hff_data_studio.repository.DatasetRepository;
import com.henri_fraise.hff_data_studio.repository.DatasetColumnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.henri_fraise.hff_data_studio.service.FileService.getString;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

	private final DatasetRepository datasetRepository;
	private final DatasetColumnRepository columnRepository;
	private final AnalysisExecutionService executionService;
	private final CleaningHistoryService cleaningHistoryService;
	private final CleaningRuleService cleaningRuleService;
	private final ExplorationReportService explorationReportService;
	private final UserService userService;
	private final ProjectService projectService;
	private final FileService fileService;

	public Map<String, Object> getGlobalStatistics() {
		Map<String, Object> stats = new HashMap<>();

		stats.put("totalDatasets", datasetRepository.count());
		stats.put("totalColumns", columnRepository.count());
		stats.put("totalProjects", projectService.countProjects());
		stats.put("totalFiles", fileService.countFiles());
		stats.put("totalUsers", userService.countUsers());
		stats.put("totalAnalyses", executionService.countAllExecutions());
		stats.put("totalCleaningOperations", cleaningHistoryService.countAllOperations());

		Long totalRows = datasetRepository.sumRowCount();
		stats.put("totalRows", totalRows != null ? totalRows : 0);

		stats.put("averageQualityScore", explorationReportService.getAverageQualityScore());
		stats.put("averageExecutionTime", executionService.getAverageExecutionTime());
		stats.put("averageCleaningDuration", cleaningHistoryService.getAverageSuccessfulCleaningDuration());
		stats.put("overallSuccessRate", cleaningHistoryService.getOverallSuccessRate());

		stats.put("activeUsers", userService.countActiveUsers());
		stats.put("completedProjects", projectService.countProjectsByStatus(ProjectStatus.COMPLETED));
		stats.put("archivedProjects", projectService.countProjectsByStatus(ProjectStatus.ARCHIVED));

		return stats;
	}

	public Map<String, Object> getDatasetStatistics(UUID datasetId) {
		Dataset dataset = datasetRepository.findById(datasetId).orElse(null);
		if (dataset == null) {
			return Map.of("error", "Dataset not found");
		}

		Map<String, Object> stats = new HashMap<>();
		stats.put("datasetId", datasetId);
		stats.put("datasetName", dataset.getDatasetName());
		stats.put("rowCount", dataset.getRowCount());
		stats.put("columnCount", dataset.getColumnCount());
		stats.put("isCleaned", dataset.getIsCleaned());
		stats.put("createdAt", dataset.getCreatedAt());

		long analysisCount = executionService.countExecutionsByDataset(datasetId);
		stats.put("analysisCount", analysisCount);

		long cleaningCount = cleaningHistoryService.countByDataset(datasetId);
		stats.put("cleaningCount", cleaningCount);

		long successCount = cleaningHistoryService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.SUCCESS);
		stats.put("successfulCleanings", successCount);

		long failureCount = cleaningHistoryService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.FAILURE);
		stats.put("failedCleanings", failureCount);

		if (cleaningCount > 0) {
			double successRate = (double) successCount / cleaningCount * 100;
			stats.put("cleaningSuccessRate", Math.round(successRate * 100.0) / 100.0);
		}

		Double avgQuality = explorationReportService.getAverageQualityScoreForDataset(datasetId);
		stats.put("averageQualityScore", avgQuality != null ? avgQuality : 0);

		List<DatasetColumn> columns = columnRepository.findByDatasetIdOrderByPositionAsc(datasetId);
		int totalNulls = columns.stream().mapToInt(DatasetColumn::getNullCount).sum();
		int totalUnique = columns.stream().mapToInt(DatasetColumn::getUniqueCount).sum();

		stats.put("totalNullValues", totalNulls);
		stats.put("totalUniqueValues", totalUnique);

		if (dataset.getRowCount() > 0) {
			double nullPercentage = (double) totalNulls / (dataset.getRowCount() * dataset.getColumnCount()) * 100;
			stats.put("nullPercentage", Math.round(nullPercentage * 100.0) / 100.0);
		}

		return stats;
	}

	public Map<String, Object> getUserStatistics(UUID userId) {
		Map<String, Object> stats = new HashMap<>();
		stats.put("userId", userId);
		stats.put("totalProjects", projectService.countProjectsByUser(userId));
		stats.put("totalFiles", fileService.countFilesByUser(userId));
		stats.put("totalAnalyses", executionService.countExecutionsByUser(userId));
		stats.put("totalCleaningOperations", cleaningHistoryService.countByUser(userId));

		long successCount = cleaningHistoryService.countByUserAndStatus(userId, CleaningHistoryStatus.SUCCESS);
		long totalCleanings = cleaningHistoryService.countByUser(userId);

		if (totalCleanings > 0) {
			double successRate = (double) successCount / totalCleanings * 100;
			stats.put("cleaningSuccessRate", Math.round(successRate * 100.0) / 100.0);
		}

		Double avgExecutionTime = executionService.getAverageExecutionTimeByUser(userId);
		stats.put("averageExecutionTimeMs", avgExecutionTime != null ? avgExecutionTime : 0);

		return stats;
	}

	public Map<String, Object> getTimeSeriesStatistics(String period, LocalDateTime startDate, LocalDateTime endDate) {
		Map<String, Object> stats = new HashMap<>();

		long datasetCount = datasetRepository.countByCreatedAtBetween(startDate, endDate);
		long analysisCount = executionService.countExecutionsBetween(startDate, endDate);
		long cleaningCount = cleaningHistoryService.countCleaningOperationsBetween(startDate, endDate);
		long userCount = userService.countUsersCreatedBetween(startDate, endDate);
		long projectCount = projectService.countProjectsCreatedBetween(startDate, endDate);

		stats.put("period", period);
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("newDatasets", datasetCount);
		stats.put("newAnalyses", analysisCount);
		stats.put("newCleanings", cleaningCount);
		stats.put("newUsers", userCount);
		stats.put("newProjects", projectCount);

		return stats;
	}

	public Map<String, Object> getColumnStatistics(UUID columnId) {
		DatasetColumn column = columnRepository.findById(columnId).orElse(null);
		if (column == null) {
			return Map.of("error", "Column not found");
		}

		Map<String, Object> stats = new HashMap<>();
		stats.put("columnId", columnId);
		stats.put("originalName", column.getOriginalName());
		stats.put("normalizedName", column.getNormalizedName());
		stats.put("detectedType", column.getDetectedType());
		stats.put("targetType", column.getTargetType());
		stats.put("nullCount", column.getNullCount());
		stats.put("uniqueCount", column.getUniqueCount());

		Dataset dataset = column.getDataset();
		if (dataset != null) {
			stats.put("datasetId", dataset.getId());
			stats.put("datasetName", dataset.getDatasetName());
			stats.put("rowCount", dataset.getRowCount());

			if (dataset.getRowCount() > 0) {
				DataExplorationService.calculateNullPercentage(column, stats, dataset);
			}
		}

		long ruleCount = cleaningRuleService.countRulesByColumn(columnId);
		stats.put("cleaningRuleCount", ruleCount);

		long cleaningHistoryCount = cleaningHistoryService.countByColumn(columnId);
		stats.put("cleaningHistoryCount", cleaningHistoryCount);

		return stats;
	}

	public Map<String, Object> getStorageStatistics() {
		Map<String, Object> stats = new HashMap<>();

		long totalFileSize = fileService.getTotalFileSize();
		stats.put("totalFileSizeBytes", totalFileSize);
		stats.put("totalFileSizeFormatted", formatFileSize(totalFileSize));

		long averageFileSize = fileService.getAverageFileSize();
		stats.put("averageFileSizeBytes", averageFileSize);

		long totalFiles = fileService.countFiles();
		stats.put("totalFiles", totalFiles);

		return stats;
	}

	private String formatFileSize(long bytes) {
		return getString(bytes);
	}
}