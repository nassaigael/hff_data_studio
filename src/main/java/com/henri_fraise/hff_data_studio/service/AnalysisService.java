package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.request.PredefinedAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisResultResponse;
import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.AnalysisExecutionMapper;
import com.henri_fraise.hff_data_studio.mapper.AnalysisResultMapper;
import com.henri_fraise.hff_data_studio.mapper.PredefinedAnalysisMapper;
import com.henri_fraise.hff_data_studio.repository.AnalysisExecutionRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

  private final AnalysisExecutionRepository executionRepository;
  private final AnalysisExecutionMapper executionMapper;
  private final AnalysisResultMapper resultMapper;
  private final PredefinedAnalysisMapper analysisMapper;
  private final DatasetService datasetService;
  private final PredefinedAnalysisService predefinedAnalysisService;
  private final AnalysisExecutionService executionService;
  private final AnalysisResultService resultService;
  private final UserService userService;
  private final SecurityUtils securityUtils;
  private final PythonAnalysisService pythonAnalysisService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  // ==================== PREDEFINED ANALYSIS METHODS ====================

  public List<PredefinedAnalysisResponse> getPredefinedAnalyses(AnalysisCategory category) {
    try {
      if (category != null) {
        return predefinedAnalysisService.getAnalysesByCategory(category);
      }
      return predefinedAnalysisService.getAllAnalyses();
    } catch (Exception ex) {
      log.error("Error getting predefined analyses: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get predefined analyses", ex);
    }
  }

  public PredefinedAnalysisResponse getPredefinedAnalysisById(UUID analysisId) {
    try {
      return predefinedAnalysisService.getAnalysisById(analysisId);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting predefined analysis: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get predefined analysis", ex);
    }
  }

  @Transactional
  public PredefinedAnalysisResponse createPredefinedAnalysis(PredefinedAnalysisRequest request) {
    try {
      PredefinedAnalysis analysis = predefinedAnalysisService.createAnalysis(request);
      log.info("Predefined analysis created: {}", analysis.getAnalysisName());

      auditLogService.logAction(
          "PREDEFINED_ANALYSIS_CREATED",
          "PredefinedAnalysis",
          analysis.getId(),
          "Predefined analysis created: " + analysis.getAnalysisName());

      return analysisMapper.toResponse(analysis);
    } catch (Exception ex) {
      log.error("Error creating predefined analysis: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create predefined analysis", ex);
    }
  }

  @Transactional
  public PredefinedAnalysisResponse updatePredefinedAnalysis(
      UUID analysisId, PredefinedAnalysisRequest request) {
    try {
      PredefinedAnalysis analysis = predefinedAnalysisService.updateAnalysis(analysisId, request);
      log.info("Predefined analysis updated: {}", analysisId);

      auditLogService.logAction(
          "PREDEFINED_ANALYSIS_UPDATED",
          "PredefinedAnalysis",
          analysisId,
          "Predefined analysis updated");

      return analysisMapper.toResponse(analysis);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error updating predefined analysis: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update predefined analysis", ex);
    }
  }

  @Transactional
  public void deletePredefinedAnalysis(UUID analysisId) {
    try {
      predefinedAnalysisService.deleteAnalysis(analysisId);
      log.info("Predefined analysis deleted: {}", analysisId);

      auditLogService.logAction(
          "PREDEFINED_ANALYSIS_DELETED",
          "PredefinedAnalysis",
          analysisId,
          "Predefined analysis deleted");
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error deleting predefined analysis: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete predefined analysis", ex);
    }
  }

  public List<AnalysisCategory> getAllCategories() {
    try {
      return predefinedAnalysisService.getAllCategories();
    } catch (Exception ex) {
      log.error("Error getting analysis categories: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get analysis categories", ex);
    }
  }

  // ==================== EXECUTION METHODS ====================

  @Transactional
  public AnalysisExecutionResponse runAnalysis(AnalysisExecutionRequest request) {
    try {
      Dataset dataset = datasetService.getDatasetEntityById(request.getDatasetId());
      User user = userService.getUserEntityById(securityUtils.getCurrentUserId());

      PredefinedAnalysis analysis = null;
      if (request.getAnalysisId() != null) {
        analysis = predefinedAnalysisService.getAnalysisEntityById(request.getAnalysisId());
      }

      AnalysisExecution execution =
          AnalysisExecution.builder()
              .status(AnalysisExecutionStatus.PENDING)
              .usedParametersJson(request.getParameters().toString())
              .dataset(dataset)
              .predefinedAnalysis(analysis)
              .user(user)
              .build();

      AnalysisExecution saved = executionRepository.save(execution);
      log.info(
          "Analysis execution created: {} for dataset: {}",
          saved.getId(),
          dataset.getDatasetName());

      auditLogService.logAction(
          "ANALYSIS_EXECUTION_CREATED",
          "AnalysisExecution",
          saved.getId(),
          "Analysis execution created for dataset: " + dataset.getDatasetName());

      executeAnalysisAsync(saved.getId());

      return executionMapper.toResponse(saved);

    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error running analysis: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to run analysis", ex);
    }
  }

  @Async
  @Transactional
  public CompletableFuture<Void> executeAnalysisAsync(UUID executionId) {
    try {
      AnalysisExecution execution = executionService.getExecutionEntityById(executionId);
      execution.setStatus(AnalysisExecutionStatus.IN_PROGRESS);
      executionRepository.save(execution);

      pythonAnalysisService.executeAnalysis(executionId);

      return CompletableFuture.completedFuture(null);

    } catch (Exception e) {
      log.error("Error executing analysis asynchronously: {}", e.getMessage(), e);
      executionService.updateExecutionStatus(
          executionId, AnalysisExecutionStatus.ERROR, e.getMessage());
      return CompletableFuture.failedFuture(e);
    }
  }

  @Transactional
  public AnalysisExecutionResponse executeAnalysisSync(UUID executionId) {
    try {
      AnalysisExecution execution = executionService.getExecutionEntityById(executionId);
      long startTime = System.currentTimeMillis();

      execution.setStatus(AnalysisExecutionStatus.IN_PROGRESS);
      executionRepository.save(execution);

      pythonAnalysisService.executeAnalysis(executionId);

      long duration = System.currentTimeMillis() - startTime;
      execution.setDurationMs((int) duration);
      execution.setStatus(AnalysisExecutionStatus.COMPLETED);
      executionRepository.save(execution);

      log.info("Analysis execution completed: {} in {}ms", executionId, duration);

      auditLogService.logAction(
          "ANALYSIS_EXECUTION_COMPLETED",
          "AnalysisExecution",
          executionId,
          "Analysis execution completed in " + duration + "ms");

      return executionMapper.toResponse(execution);

    } catch (Exception e) {
      log.error("Error executing analysis: {}", e.getMessage(), e);
      executionService.updateExecutionStatus(
          executionId, AnalysisExecutionStatus.ERROR, e.getMessage());
      throw new DatabaseException("Failed to execute analysis", e);
    }
  }

  // ==================== EXECUTION GET METHODS ====================

  public Page<AnalysisExecutionResponse> getExecutions(UUID datasetId, Pageable pageable) {
    try {
      return executionService.getExecutions(datasetId, pageable);
    } catch (Exception ex) {
      log.error("Error getting executions: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get executions", ex);
    }
  }

  public AnalysisExecutionResponse getExecutionById(UUID executionId) {
    try {
      return executionService.getExecutionById(executionId);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting execution: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get execution", ex);
    }
  }

  public List<AnalysisResultResponse> getExecutionResults(UUID executionId) {
    try {
      List<AnalysisResult> results =
          resultService.getResultsByExecutionEntity(
              executionService.getExecutionEntityById(executionId));
      return results.stream().map(resultMapper::toResponse).toList();
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting execution results: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get execution results", ex);
    }
  }

  // ==================== RESULT METHODS ====================

  public AnalysisResultResponse getResultById(UUID resultId) {
    try {
      return resultService.getResultById(resultId);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting result: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get result", ex);
    }
  }

  public byte[] downloadResult(UUID resultId) {
    try {
      return resultService.downloadResult(resultId).getBody();
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error downloading result: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to download result", ex);
    }
  }

  // ==================== EXECUTION CONTROL METHODS ====================

  @Transactional
  public void cancelExecution(UUID executionId) {
    try {
      AnalysisExecution execution = executionService.getExecutionEntityById(executionId);

      if (execution.getStatus() == AnalysisExecutionStatus.IN_PROGRESS
          || execution.getStatus() == AnalysisExecutionStatus.PENDING) {
        execution.setStatus(AnalysisExecutionStatus.CANCELLED);
        executionRepository.save(execution);
        pythonAnalysisService.cancelExecution(executionId);

        log.info("Execution cancelled: {}", executionId);

        auditLogService.logAction(
            "ANALYSIS_EXECUTION_CANCELLED",
            "AnalysisExecution",
            executionId,
            "Analysis execution cancelled");
      } else {
        throw new IllegalStateException(
            "Cannot cancel execution with status: " + execution.getStatus());
      }
    } catch (Exception ex) {
      log.error("Error cancelling execution: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to cancel execution", ex);
    }
  }

  @Transactional
  public void deleteExecution(UUID executionId) {
    try {
      AnalysisExecution execution = executionService.getExecutionEntityById(executionId);

      if (execution.getStatus() == AnalysisExecutionStatus.IN_PROGRESS) {
        throw new IllegalStateException("Cannot delete execution while it is in progress");
      }

      resultService.deleteResultsByExecution(executionId);
      executionService.deleteExecution(executionId);

      log.info("Execution deleted: {}", executionId);

      auditLogService.logAction(
          "ANALYSIS_EXECUTION_DELETED",
          "AnalysisExecution",
          executionId,
          "Analysis execution deleted");
    } catch (Exception ex) {
      log.error("Error deleting execution: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete execution", ex);
    }
  }

  // ==================== MODEL SAVE METHODS ====================

  @Transactional
  public Map<String, Object> saveAnalysisModel(Map<String, Object> request) {
    try {
      String name = (String) request.get("name");
      UUID analysisId = UUID.fromString((String) request.get("analysisId"));
      Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");

      PredefinedAnalysis analysis = predefinedAnalysisService.getAnalysisEntityById(analysisId);

      Map<String, Object> model = new HashMap<>();
      model.put("name", name);
      model.put("analysisId", analysisId);
      model.put("analysisName", analysis.getAnalysisName());
      model.put("parameters", parameters);
      model.put("savedAt", LocalDateTime.now());
      model.put("userId", securityUtils.getCurrentUserId());

      log.info("Analysis model saved: {}", name);

      auditLogService.logAction(
          "ANALYSIS_MODEL_SAVED",
          "PredefinedAnalysis",
          analysisId,
          "Analysis model saved: " + name);

      return model;
    } catch (Exception ex) {
      log.error("Error saving analysis model: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to save analysis model", ex);
    }
  }

  // ==================== STATISTICS METHODS ====================

  public Map<String, Object> getExecutionStatistics(UUID executionId) {
    try {
      return executionService.getExecutionStatistics(executionId);
    } catch (ResourceNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error("Error getting execution statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get execution statistics", ex);
    }
  }

  public Map<String, Object> getGlobalAnalysisStatistics() {
    try {
      Map<String, Object> stats = new HashMap<>();
      stats.put("totalExecutions", executionService.countAllExecutions());
      stats.put(
          "totalCompleted",
          executionService.countExecutionsByStatus(AnalysisExecutionStatus.COMPLETED));
      stats.put(
          "totalInProgress",
          executionService.countExecutionsByStatus(AnalysisExecutionStatus.IN_PROGRESS));
      stats.put(
          "totalError", executionService.countExecutionsByStatus(AnalysisExecutionStatus.ERROR));
      stats.put(
          "totalCancelled",
          executionService.countExecutionsByStatus(AnalysisExecutionStatus.CANCELLED));
      stats.put(
          "totalPending",
          executionService.countExecutionsByStatus(AnalysisExecutionStatus.PENDING));
      stats.put("averageExecutionTimeMs", executionService.getAverageExecutionTime());
      stats.put("totalPredefinedAnalyses", predefinedAnalysisService.countAnalyses());

      return stats;
    } catch (Exception ex) {
      log.error("Error getting global analysis statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get global analysis statistics", ex);
    }
  }

  public Map<String, Object> getAnalysisCategoryStatistics() {
    try {
      Map<String, Object> stats = new HashMap<>();
      for (AnalysisCategory category : AnalysisCategory.values()) {
        long count = predefinedAnalysisService.countAnalysesByCategory(category);
        stats.put(category.name().toLowerCase(), count);
      }
      return stats;
    } catch (Exception ex) {
      log.error("Error getting analysis category statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get analysis category statistics", ex);
    }
  }

  // ==================== VALIDATION METHODS ====================

  public boolean existsAnalysisById(UUID analysisId) {
    try {
      return predefinedAnalysisService.getAnalysisEntityById(analysisId) != null;
    } catch (ResourceNotFoundException e) {
      return false;
    }
  }

  public boolean existsExecutionById(UUID executionId) {
    try {
      return executionService.getExecutionEntityById(executionId) != null;
    } catch (ResourceNotFoundException e) {
      return false;
    }
  }

  public boolean isExecutionCompleted(UUID executionId) {
    try {
      return executionService.isExecutionCompleted(executionId);
    } catch (Exception ex) {
      log.error("Error checking execution status: {}", ex.getMessage(), ex);
      return false;
    }
  }

  // ==================== UTILITY METHODS ====================

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

  public long countExecutionsByDataset(UUID datasetId) {
    return executionService.countExecutionsByDataset(datasetId);
  }

  public long countExecutionsByUser(UUID userId) {
    return executionService.countExecutionsByUser(userId);
  }
}
