package com.henri_fraise.hff_data_studio.service.helper;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.repository.AnalysisExecutionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisExecutionHelper {

  private final AnalysisExecutionRepository executionRepository;

  public AnalysisExecution findById(UUID executionId) {
    return executionRepository
        .findById(executionId)
        .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));
  }

  @Transactional
  public void updateStatus(UUID executionId, AnalysisExecutionStatus status) {
    AnalysisExecution execution = findById(executionId);
    execution.setStatus(status);
    executionRepository.save(execution);
  }

  @Transactional
  public void updateStatusWithDuration(
      UUID executionId, AnalysisExecutionStatus status, Integer durationMs) {
    AnalysisExecution execution = findById(executionId);
    execution.setStatus(status);
    if (durationMs != null) {
      execution.setDurationMs(durationMs);
    }
    executionRepository.save(execution);
  }

  @Transactional
  public void updateWithResults(
      UUID executionId, AnalysisExecutionStatus status, Integer durationMs, String usedParameters) {
    AnalysisExecution execution = findById(executionId);
    execution.setStatus(status);
    if (durationMs != null) {
      execution.setDurationMs(durationMs);
    }
    if (usedParameters != null) {
      execution.setUsedParametersJson(usedParameters.toString());
    }
    executionRepository.save(execution);
  }

  @Transactional
  public void updateStatusWithMessage(
      UUID executionId, AnalysisExecutionStatus status, String errorMessage) {
    AnalysisExecution execution = findById(executionId);
    execution.setStatus(status);
    executionRepository.save(execution);
    log.error("Execution failed: {} - {}", executionId, errorMessage);
  }
}
