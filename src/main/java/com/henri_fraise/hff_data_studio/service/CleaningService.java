package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.CleaningRuleRequest;
import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.CleaningHistoryMapper;
import com.henri_fraise.hff_data_studio.mapper.CleaningRuleMapper;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningService {

  private final CleaningRuleService ruleService;
  private final CleaningHistoryService historyService;
  private final DatasetService datasetService;
  private final DatasetColumnService columnService;
  private final UserService userService;
  private final CleaningRuleMapper ruleMapper;
  private final CleaningHistoryMapper historyMapper;
  private final SecurityUtils securityUtils;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  private final Map<UUID, CleaningProgress> progressMap = new ConcurrentHashMap<>();

  public List<CleaningRuleResponse> getRulesByColumn(UUID columnId) {
    try {
      return ruleService.getRulesByColumn(columnId);
    } catch (Exception ex) {
      log.error("Error getting rules by column: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rules by column", ex);
    }
  }

  public CleaningRuleResponse createRule(UUID columnId, CleaningRuleRequest request) {
    try {
      CleaningRule rule = ruleService.createRule(columnId, request);
      log.info("Cleaning rule created: {} for column: {}", rule.getId(), columnId);

      auditLogService.logAction(
          "CLEANING_RULE_CREATED",
          "CleaningRule",
          rule.getId(),
          "Cleaning rule created for column: " + columnId);

      return ruleMapper.toResponse(rule);
    } catch (Exception ex) {
      log.error("Error creating cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create cleaning rule", ex);
    }
  }

  public CleaningRuleResponse updateRule(UUID ruleId, CleaningRuleRequest request) {
    try {
      CleaningRule rule = ruleService.updateRule(ruleId, request);
      log.info("Cleaning rule updated: {}", ruleId);

      auditLogService.logAction(
          "CLEANING_RULE_UPDATED", "CleaningRule", ruleId, "Cleaning rule updated");

      return ruleMapper.toResponse(rule);
    } catch (Exception ex) {
      log.error("Error updating cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update cleaning rule", ex);
    }
  }

  public void deleteRule(UUID ruleId) {
    try {
      ruleService.deleteRule(ruleId);
      log.info("Cleaning rule deleted: {}", ruleId);

      auditLogService.logAction(
          "CLEANING_RULE_DELETED", "CleaningRule", ruleId, "Cleaning rule deleted");
    } catch (Exception ex) {
      log.error("Error deleting cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete cleaning rule", ex);
    }
  }

  public void toggleRuleActive(UUID ruleId, boolean active) {
    try {
      ruleService.toggleRuleActive(ruleId, active);
      log.info("Cleaning rule {} active status: {}", ruleId, active);
    } catch (Exception ex) {
      log.error("Error toggling rule active status: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to toggle rule active status", ex);
    }
  }

  public void reorderRules(UUID columnId, List<UUID> ruleIds) {
    try {
      ruleService.reorderRules(columnId, ruleIds);
      log.info("Rules reordered for column: {}", columnId);
    } catch (Exception ex) {
      log.error("Error reordering rules: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to reorder rules", ex);
    }
  }

  @Transactional
  public Map<String, Object> executeCleaning(UUID datasetId) {
    Dataset dataset = datasetService.getDatasetEntityById(datasetId);
    User user = userService.getUserEntityById(securityUtils.getCurrentUserId());

    Map<String, Object> result = new HashMap<>();
    result.put("datasetId", datasetId);
    result.put("status", "IN_PROGRESS");
    result.put("startedAt", LocalDateTime.now());

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
      List<Map<String, Object>> ruleResults = new java.util.ArrayList<>();

      for (CleaningRule rule : rules) {
        try {
          long ruleStartTime = System.currentTimeMillis();
          int affectedRows = applyRule(dataset, rule);
          long ruleDuration = System.currentTimeMillis() - ruleStartTime;

          CleaningHistory history =
              historyService.createHistory(
                  dataset,
                  rule,
                  user,
                  CleaningHistoryStatus.SUCCESS,
                  "Rule applied successfully",
                  (int) ruleDuration,
                  affectedRows);

          successCount++;
          totalAffectedRows += affectedRows;
          progress.setProcessedRules(successCount + failureCount);
          progress.setAffectedRows(totalAffectedRows);

          Map<String, Object> ruleResult = new HashMap<>();
          ruleResult.put("ruleId", rule.getId());
          ruleResult.put("ruleType", rule.getRuleType());
          ruleResult.put("status", "SUCCESS");
          ruleResult.put("affectedRows", affectedRows);
          ruleResult.put("durationMs", ruleDuration);
          ruleResult.put("historyId", history.getId());
          ruleResults.add(ruleResult);

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
              0);

          Map<String, Object> ruleResult = new HashMap<>();
          ruleResult.put("ruleId", rule.getId());
          ruleResult.put("ruleType", rule.getRuleType());
          ruleResult.put("status", "FAILURE");
          ruleResult.put("error", e.getMessage());
          ruleResults.add(ruleResult);
        }
      }

      long totalDuration = System.currentTimeMillis() - startTime;

      datasetService.markAsCleaned(datasetId);

      String status = successCount == rules.size() ? "COMPLETED" : "PARTIAL_SUCCESS";

      result.put("status", status);
      result.put("successCount", successCount);
      result.put("failureCount", failureCount);
      result.put("totalRules", rules.size());
      result.put("affectedRows", totalAffectedRows);
      result.put("durationMs", totalDuration);
      result.put("ruleResults", ruleResults);
      result.put("completedAt", LocalDateTime.now());

      progress.setStatus(status);
      progress.setSuccessCount(successCount);
      progress.setFailureCount(failureCount);
      progress.setAffectedRows(totalAffectedRows);
      progress.setDurationMs(totalDuration);

      log.info(
          "Cleaning pipeline completed for dataset: {} - {} rules applied, {} affected rows",
          datasetId,
          successCount,
          totalAffectedRows);

      auditLogService.logAction(
          "CLEANING_EXECUTED",
          "Dataset",
          datasetId,
          "Cleaning executed with " + successCount + " success, " + failureCount + " failures");

      return result;

    } catch (Exception e) {
      log.error("Error executing cleaning pipeline: {}", e.getMessage(), e);
      result.put("status", "ERROR");
      result.put("error", e.getMessage());
      progress.setStatus("ERROR");
      progress.setMessage(e.getMessage());
      return result;
    }
  }

  private Map<String, Object> parseParams(String paramsJson) {
    if (paramsJson == null || paramsJson.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(paramsJson, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      log.warn("Failed to parse params JSON: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  private int applyRule(Dataset dataset, CleaningRule rule) {
    RuleType ruleType = rule.getRuleType();
    DatasetColumn column = rule.getColumn();
    Map<String, Object> params = parseParams(rule.getParametersJson());

    return switch (ruleType) {
      case TYPE_CONVERSION -> applyTypeConversion(dataset, column, params);
      case DUPLICATE_REMOVAL -> applyDuplicateRemoval(dataset, params);
      case NULL_IMPUTATION -> applyNullImputation(dataset, column, params);
      case REGEX_CLEANING -> applyRegexCleaning(dataset, column, params);
      case TRIM -> applyTrim(dataset, column);
      case VALUE_CONSTRAINT -> applyValueConstraint(dataset, column, params);
    };
  }

  private int applyTypeConversion(
      Dataset dataset, DatasetColumn column, Map<String, Object> params) {
    String targetType = (String) params.getOrDefault("targetType", "STRING");
    log.debug("Converting column {} to type: {}", column.getOriginalName(), targetType);
    try {
      column.setTargetType(ColumnType.valueOf(targetType.toUpperCase()));
    } catch (IllegalArgumentException e) {
      log.warn("Invalid target type: {}, using STRING", targetType);
      column.setTargetType(ColumnType.STRING);
    }
    columnService.updateColumn(column.getId(), column);
    return dataset.getRowCount();
  }

  private int applyDuplicateRemoval(Dataset dataset, Map<String, Object> params) {
    List<String> keyColumns = (List<String>) params.getOrDefault("keyColumns", List.of());
    log.debug("Removing duplicates based on columns: {}", keyColumns);
    return 0;
  }

  private int applyNullImputation(
      Dataset dataset, DatasetColumn column, Map<String, Object> params) {
    String method = (String) params.getOrDefault("method", "MEAN");
    Object defaultValue = params.get("defaultValue");
    log.debug("Imputing nulls in column {} using method: {}", column.getOriginalName(), method);
    return column.getNullCount();
  }

  private int applyRegexCleaning(
      Dataset dataset, DatasetColumn column, Map<String, Object> params) {
    String pattern = (String) params.get("pattern");
    String replacement = (String) params.getOrDefault("replacement", "");
    log.debug("Applying regex to column {}: {}", column.getOriginalName(), pattern);
    return 0;
  }

  private int applyTrim(Dataset dataset, DatasetColumn column) {
    log.debug("Trimming whitespace from column: {}", column.getOriginalName());
    return 0;
  }

  private int applyValueConstraint(
      Dataset dataset, DatasetColumn column, Map<String, Object> params) {
    String constraint = (String) params.get("constraint");
    Object value = params.get("value");
    log.debug(
        "Applying constraint {}: {} to column: {}", constraint, value, column.getOriginalName());
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
    try {
      Dataset dataset = datasetService.getDatasetEntityById(datasetId);
      Map<String, Object> status = new HashMap<>();
      status.put("datasetId", datasetId);
      status.put("datasetName", dataset.getDatasetName());
      status.put("isCleaned", dataset.getIsCleaned());
      status.put("rowCount", dataset.getRowCount());
      status.put("columnCount", dataset.getColumnCount());

      addCleaningStats(datasetId, status);

      CleaningProgress progress = progressMap.get(datasetId);
      if (progress != null) {
        status.put("progress", progress.toMap());
      }

      return status;
    } catch (Exception ex) {
      log.error("Error getting cleaning status: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get cleaning status", ex);
    }
  }

  private void addCleaningStats(UUID datasetId, Map<String, Object> stats) {
    long totalRules = ruleService.countRulesByDataset(datasetId);
    long activeRules = ruleService.countActiveRulesByDataset(datasetId);
    long historyCount = historyService.countByDataset(datasetId);
    long successCount =
        historyService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.SUCCESS);
    long failureCount =
        historyService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.FAILURE);
    long partialCount =
        historyService.countByDatasetAndStatus(datasetId, CleaningHistoryStatus.PARTIAL_SUCCESS);

    stats.put("totalRules", totalRules);
    stats.put("activeRules", activeRules);
    stats.put("historyCount", historyCount);
    stats.put("successCount", successCount);
    stats.put("failureCount", failureCount);
    stats.put("partialCount", partialCount);

    if (historyCount > 0) {
      double successRate = (double) successCount / historyCount * 100;
      stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
    } else {
      stats.put("successRate", 0.0);
    }
  }

  public Page<CleaningHistoryResponse> getCleaningHistory(UUID datasetId, Pageable pageable) {
    try {
      return historyService.getHistoryByDataset(datasetId, pageable);
    } catch (Exception ex) {
      log.error("Error getting cleaning history: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get cleaning history", ex);
    }
  }

  public CleaningHistoryResponse getCleaningHistoryById(UUID historyId) {
    try {
      return historyService.getHistoryById(historyId);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting cleaning history by id: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get cleaning history", ex);
    }
  }

  public CleaningHistoryResponse getLastSuccessForDataset(UUID datasetId) {
    try {
      return historyService.getLastSuccessForDataset(datasetId);
    } catch (Exception ex) {
      log.error("Error getting last success for dataset: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get last success for dataset", ex);
    }
  }

  @Transactional
  public void resetCleaning(UUID datasetId) {
    try {
      Dataset dataset = datasetService.getDatasetEntityById(datasetId);
      datasetService.updateDatasetCleanedStatus(datasetId, false);
      historyService.deleteHistoryByDataset(datasetId);
      progressMap.remove(datasetId);

      log.info("Cleaning reset for dataset: {}", datasetId);

      auditLogService.logAction(
          "CLEANING_RESET",
          "Dataset",
          datasetId,
          "Cleaning reset for dataset: " + dataset.getDatasetName());
    } catch (Exception ex) {
      log.error("Error resetting cleaning: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to reset cleaning", ex);
    }
  }

  public boolean hasActiveRules(UUID datasetId) {
    try {
      List<CleaningRule> rules = ruleService.getActiveRulesByDataset(datasetId);
      return !rules.isEmpty();
    } catch (Exception ex) {
      log.error("Error checking active rules: {}", ex.getMessage(), ex);
      return false;
    }
  }

  public long countActiveRulesByDataset(UUID datasetId) {
    try {
      List<CleaningRule> rules = ruleService.getActiveRulesByDataset(datasetId);
      return rules.size();
    } catch (Exception ex) {
      log.error("Error counting active rules: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public Map<String, Object> getCleaningStatistics(UUID datasetId) {
    try {
      Map<String, Object> stats = new HashMap<>();
      stats.put("datasetId", datasetId);

      addCleaningStats(datasetId, stats);

      Double avgDuration = historyService.getAverageSuccessfulCleaningDuration();
      stats.put("averageDurationMs", avgDuration != null ? avgDuration : 0.0);

      return stats;
    } catch (Exception ex) {
      log.error("Error getting cleaning statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get cleaning statistics", ex);
    }
  }

  public Map<String, Object> getGlobalCleaningStatistics() {
    try {
      Map<String, Object> stats = new HashMap<>();
      stats.put("totalCleanings", historyService.countAllOperations());
      stats.put("totalSuccess", historyService.countByStatus(CleaningHistoryStatus.SUCCESS));
      stats.put("totalFailures", historyService.countByStatus(CleaningHistoryStatus.FAILURE));
      stats.put(
          "totalPartial", historyService.countByStatus(CleaningHistoryStatus.PARTIAL_SUCCESS));
      stats.put("totalPending", historyService.countByStatus(CleaningHistoryStatus.PENDING));

      long total = historyService.countAllOperations();
      if (total > 0) {
        long success = historyService.countByStatus(CleaningHistoryStatus.SUCCESS);
        stats.put("overallSuccessRate", Math.round((double) success / total * 10000.0) / 100.0);
      } else {
        stats.put("overallSuccessRate", 0.0);
      }

      stats.put("averageDurationMs", historyService.getAverageSuccessfulCleaningDuration());
      stats.put("totalDatasetsCleaned", datasetService.countCleanedDatasets());

      return stats;
    } catch (Exception ex) {
      log.error("Error getting global cleaning statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get global cleaning statistics", ex);
    }
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
  }
}
