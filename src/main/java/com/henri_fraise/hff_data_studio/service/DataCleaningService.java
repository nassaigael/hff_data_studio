package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleaningService {

	private final DatasetService datasetService;
	private final DatasetColumnService columnService;
	private final CleaningRuleService ruleService;
	private final CleaningHistoryService historyService;
	private final UserService userService;
	private final SecurityUtils securityUtils;

	private final Map<UUID, CleaningProgress> progressMap = new ConcurrentHashMap<>();

	@Transactional
	public Map<String, Object> executeCleaningPipeline(UUID datasetId) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		User user = userService.getUserEntityById(securityUtils.getCurrentUserId());

		Map<String, Object> result = new HashMap<>();
		result.put("datasetId", datasetId);
		result.put("status", "IN_PROGRESS");

		CleaningProgress progress = new CleaningProgress();
		progress.setDatasetId(datasetId);
		progress.setStatus("IN_PROGRESS");
		progressMap.put(datasetId, progress);

		long startTime = System.currentTimeMillis();

		try {
			List<CleaningRule> rules = ruleService.getActiveRulesByDataset(datasetId);
			progress.setTotalRules(rules.size());

			if (rules.isEmpty()) {
				result.put("status", "COMPLETED");
				result.put("message", "No active rules to apply");
				progress.setStatus("COMPLETED");
				progress.setMessage("No active rules to apply");
				return result;
			}

			int successCount = 0;
			int failureCount = 0;
			int totalAffectedRows = 0;

			for (CleaningRule rule : rules) {
				try {
					long ruleStartTime = System.currentTimeMillis();
					int affectedRows = applyRule(dataset, rule);
					long ruleDuration = System.currentTimeMillis() - ruleStartTime;

					historyService.createHistory(
							dataset,
							rule,
							user,
							CleaningHistoryStatus.SUCCESS,
							"Rule applied successfully",
							(int) ruleDuration,
							affectedRows
					);

					successCount++;
					totalAffectedRows += affectedRows;

					progress.setProcessedRules(successCount + failureCount);
					progress.setAffectedRows(totalAffectedRows);

				} catch (Exception e) {
					log.error("Error applying rule {}: {}", rule.getId(), e.getMessage());
					failureCount++;

					historyService.createHistory(
							dataset,
							rule,
							user,
							CleaningHistoryStatus.FAILURE,
							"Error: " + e.getMessage(),
							null,
							0
					);
				}
			}

			long totalDuration = System.currentTimeMillis() - startTime;

			dataset.setIsCleaned(true);
			datasetService.updateDatasetCleanedStatus(datasetId);

			result.put("status", successCount == rules.size() ? "COMPLETED" : "PARTIAL_SUCCESS");
			result.put("successCount", successCount);
			result.put("failureCount", failureCount);
			result.put("totalRules", rules.size());
			result.put("affectedRows", totalAffectedRows);
			result.put("durationMs", totalDuration);

			progress.setStatus(result.get("status").toString());
			progress.setSuccessCount(successCount);
			progress.setFailureCount(failureCount);
			progress.setAffectedRows(totalAffectedRows);
			progress.setDurationMs(totalDuration);

			log.info("Cleaning pipeline completed for dataset: {} - {} rules applied, {} affected rows",
					datasetId, successCount, totalAffectedRows);

		} catch (Exception e) {
			log.error("Error executing cleaning pipeline: {}", e.getMessage());
			result.put("status", "ERROR");
			result.put("error", e.getMessage());
			progress.setStatus("ERROR");
			progress.setMessage(e.getMessage());
		}

		return result;
	}

	private int applyRule(Dataset dataset, CleaningRule rule) {
		RuleType ruleType = rule.getRuleType();
		DatasetColumn column = rule.getColumn();
		String params = rule.getParametersJson();

		return switch (ruleType) {
			case TYPE_CONVERSION -> applyTypeConversion(dataset, column);
			case DUPLICATE_REMOVAL -> applyDuplicateRemoval(dataset);
			case NULL_IMPUTATION -> applyNullImputation(dataset, column);
			case REGEX_CLEANING -> applyRegexCleaning(dataset, column);
			case TRIM -> applyTrim(dataset, column);
			case VALUE_CONSTRAINT -> applyValueConstraint(dataset, column);
		};
	}

	private int applyTypeConversion(Dataset dataset, DatasetColumn column) {
		String targetType = (String) ((Map<String, Object>) null).getOrDefault("targetType", "STRING");
		log.debug("Converting column {} to type: {}", column.getOriginalName(), targetType);
		column.setTargetType(ColumnType.valueOf(targetType));
		columnService.updateColumn(column.getId(), column);
		return dataset.getRowCount();
	}

	private int applyDuplicateRemoval(Dataset dataset) {
		List<String> keyColumns = (List<String>) ((Map<String, Object>) null).getOrDefault("keyColumns", List.of());
		log.debug("Removing duplicates based on columns: {}", keyColumns);
		return 0;
	}

	private int applyNullImputation(Dataset dataset, DatasetColumn column) {
		String method = (String) ((Map<String, Object>) null).getOrDefault("method", "MEAN");
		Object defaultValue = ((Map<String, Object>) null).get("defaultValue");
		log.debug("Imputing nulls in column {} using method: {}", column.getOriginalName(), method);
		return column.getNullCount();
	}

	private int applyRegexCleaning(Dataset dataset, DatasetColumn column) {
		String pattern = (String) ((Map<String, Object>) null).get("pattern");
		String replacement = (String) ((Map<String, Object>) null).getOrDefault("replacement", "");
		log.debug("Applying regex to column {}: {}", column.getOriginalName(), pattern);
		return 0;
	}

	private int applyTrim(Dataset dataset, DatasetColumn column) {
		log.debug("Trimming whitespace from column: {}", column.getOriginalName());
		return 0;
	}

	private int applyValueConstraint(Dataset dataset, DatasetColumn column) {
		String constraint = (String) ((Map<String, Object>) null).get("constraint");
		Object value = ((Map<String, Object>) null).get("value");
		log.debug("Applying constraint {}: {} to column: {}", constraint, value, column.getOriginalName());
		return 0;
	}

	public Map<String, Object> getCleaningProgress(UUID datasetId) {
		CleaningProgress progress = progressMap.get(datasetId);
		if (progress == null) {
			Map<String, Object> result = new HashMap<>();
			result.put("datasetId", datasetId);
			result.put("status", "NOT_STARTED");
			return result;
		}
		return progress.toMap();
	}

	public Map<String, Object> getCleaningStatus(UUID datasetId) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		Map<String, Object> status = new HashMap<>();
		status.put("datasetId", datasetId);
		status.put("isCleaned", dataset.getIsCleaned());
		status.put("rowCount", dataset.getRowCount());
		status.put("columnCount", dataset.getColumnCount());

		long totalRules = ruleService.countRulesByDataset(datasetId);
		long activeRules = ruleService.countActiveRulesByColumn(datasetId);
		long historyCount = historyService.countByDataset(datasetId);
		long successCount = historyService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.SUCCESS);
		long failureCount = historyService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.FAILURE);

		status.put("totalRules", totalRules);
		status.put("activeRules", activeRules);
		status.put("historyCount", historyCount);
		status.put("successCount", successCount);
		status.put("failureCount", failureCount);

		if (historyCount > 0) {
			double successRate = (double) successCount / historyCount * 100;
			status.put("successRate", Math.round(successRate * 100.0) / 100.0);
		}

		return status;
	}

	@Transactional
	public void resetCleaning(UUID datasetId) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		dataset.setIsCleaned(false);
		datasetService.updateDatasetCleanedStatus(datasetId);
		historyService.deleteHistoryByDataset(datasetId);
		progressMap.remove(datasetId);
		log.info("Cleaning reset for dataset: {}", datasetId);
	}

	@Setter
	private static class CleaningProgress {
		private UUID datasetId;
		private String status;
		private String message;
		private int totalRules;
		private int processedRules;
		private int successCount;
		private int failureCount;
		private int affectedRows;
		private Long durationMs;

		public Map<String, Object> toMap() {
			Map<String, Object> map = new HashMap<>();
			map.put("datasetId", datasetId);
			map.put("status", status);
			map.put("message", message);
			map.put("totalRules", totalRules);
			map.put("processedRules", processedRules);
			map.put("successCount", successCount);
			map.put("failureCount", failureCount);
			map.put("affectedRows", affectedRows);
			map.put("durationMs", durationMs);
			map.put("progress", totalRules > 0 ? (int) ((double) processedRules / totalRules * 100) : 0);
			return map;
		}

		private Map<String, Object> getStringObjectMap(UUID datasetId, String status, String message, int totalRules, int processedRules, int successCount, int failureCount, int affectedRows, Long durationMs) {
			Map<String, Object> map = new HashMap<>();
			map.put("datasetId", datasetId);
			map.put("status", status);
			map.put("message", message);
			map.put("totalRules", totalRules);
			map.put("processedRules", processedRules);
			map.put("successCount", successCount);
			map.put("failureCount", failureCount);
			map.put("affectedRows", affectedRows);
			map.put("durationMs", durationMs);
			map.put("progress", totalRules > 0 ? (int) ((double) processedRules / totalRules * 100) : 0);
			return map;
		}

	}
}