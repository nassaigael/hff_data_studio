package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.CleaningHistoryMapper;
import com.henri_fraise.hff_data_studio.repository.CleaningHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningHistoryService {

  private final CleaningHistoryRepository historyRepository;
  private final CleaningHistoryMapper historyMapper;

  @Transactional
  public CleaningHistory createHistory(
      Dataset dataset,
      CleaningRule rule,
      User user,
      CleaningHistoryStatus status,
      String details,
      Integer durationMs,
      Integer affectedRows) {
    CleaningHistory history =
        CleaningHistory.builder()
            .dataset(dataset)
            .rule(rule)
            .user(user)
            .status(status)
            .details(details)
            .durationMs(durationMs)
            .affectedRows(affectedRows)
            .build();
    CleaningHistory saved = historyRepository.save(history);
    log.info(
        "Cleaning history created: {} for dataset: {}", saved.getId(), dataset.getDatasetName());
    return history;
  }

  public long countAllOperations() {
    return historyRepository.count();
  }

  public long countByColumn(UUID columnId) {
    return historyRepository.countByColumnId(columnId);
  }

  public CleaningHistory getHistoryEntityById(UUID historyId) {
    return historyRepository
        .findById(historyId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cleaning history not found: " + historyId));
  }

  public CleaningHistoryResponse getHistoryById(UUID historyId) {
    CleaningHistory history = getHistoryEntityById(historyId);
    return historyMapper.toResponse(history);
  }

  public Page<CleaningHistoryResponse> getHistoryByDataset(UUID datasetId, Pageable pageable) {
    Page<CleaningHistory> histories =
        historyRepository.findByDatasetIdOrderByExecutedAtDesc(datasetId, pageable);
    return histories.map(historyMapper::toResponse);
  }

  public List<CleaningHistory> getHistoryByDatasetEntity(Dataset dataset) {
    return historyRepository.findByDatasetIdOrderByExecutedAtDesc(dataset.getId());
  }

  public Page<CleaningHistoryResponse> getHistoryByUser(UUID userId, Pageable pageable) {
    Page<CleaningHistory> histories =
        historyRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable);
    return histories.map(historyMapper::toResponse);
  }

  public List<CleaningHistory> getHistoryByStatus(CleaningHistoryStatus status) {
    return historyRepository.findByStatus(status);
  }

  public Page<CleaningHistoryResponse> getHistoryByStatus(
      CleaningHistoryStatus status, Pageable pageable) {
    Page<CleaningHistory> histories = historyRepository.findByStatus(status, pageable);
    return histories.map(historyMapper::toResponse);
  }

  public List<CleaningHistory> getHistorySince(UUID datasetId, LocalDateTime since) {
    return historyRepository.findByDatasetIdSince(datasetId, since);
  }

  public List<CleaningHistory> getHistoryByDateRange(
      LocalDateTime startDate, LocalDateTime endDate) {
    return historyRepository.findByExecutedDateRange(startDate, endDate);
  }

  public List<CleaningHistory> getOldRecordsByStatus(
      CleaningHistoryStatus status, LocalDateTime date) {
    return historyRepository.findOldRecordsByStatus(status, date);
  }

  public long countByDataset(UUID datasetId) {
    return historyRepository.countByDatasetId(datasetId);
  }

  public long countByStatus(CleaningHistoryStatus status) {
    return historyRepository.countByStatus(status);
  }

  public long countByDatasetAndStatus(UUID datasetId, CleaningHistoryStatus status) {
    return historyRepository.countByDatasetIdAndStatus(datasetId, status);
  }

  public long countByUserAndStatus(UUID userId, CleaningHistoryStatus status) {
    return historyRepository.countByUserIdAndStatus(userId, status);
  }

  public long countCleaningOperationsBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return historyRepository.countCleaningOperationBetween(startDate, endDate);
  }

  public Double getAverageSuccessfulCleaningDuration() {
    return historyRepository.averageSuccessfulCleaningDuration();
  }

  public Double getSuccessRateForDataset(UUID datasetId) {
    return historyRepository.calculateSuccessRateForDataset(datasetId);
  }

  public Double getOverallSuccessRate() {
    return historyRepository.calculateOverallSuccessRate();
  }

  public long countFailures() {
    return historyRepository.countFailures();
  }

  public long countPartialSuccess() {
    return historyRepository.countPartialSuccess();
  }

  public List<Object[]> countByDatasetGrouped() {
    return historyRepository.countByDatasetGrouped();
  }

  public List<Object[]> countByUserGrouped() {
    return historyRepository.countByUserGrouped();
  }

  public List<CleaningHistory> getLatestByDataset(UUID datasetId, int limit) {
    return historyRepository.findLatestByDatasetId(datasetId, Pageable.ofSize(limit));
  }

  public List<CleaningHistory> getLatestByStatus(CleaningHistoryStatus status, int limit) {
    return historyRepository.findLatestByStatus(status, Pageable.ofSize(limit));
  }

  public CleaningHistoryResponse getLastSuccessForDataset(UUID datasetId) {
    List<CleaningHistory> histories =
        historyRepository.findByDatasetIdAndStatus(datasetId, CleaningHistoryStatus.SUCCESS);
    if (histories.isEmpty()) return null;
    CleaningHistory lastSuccess =
        histories.stream()
            .max((h1, h2) -> h1.getExecutedAt().compareTo(h2.getExecutedAt()))
            .orElse(null);
    return historyMapper.toResponse(lastSuccess);
  }

  @Transactional
  public void updateHistoryStatus(UUID historyId, CleaningHistoryStatus status, String details) {
    CleaningHistory history = getHistoryEntityById(historyId);
    history.setStatus(status);
    if (details != null) {
      history.setDetails(details);
    }
    historyRepository.save(history);
    log.info("Cleaning history updated: {} -> {}", historyId, status);
  }

  @Transactional
  public void deleteHistory(UUID historyId) {
    CleaningHistory history = getHistoryEntityById(historyId);
    historyRepository.delete(history);
    log.info("Cleaning history deleted: {}", historyId);
  }

  @Transactional
  public void deleteHistoryByDataset(UUID datasetId) {
    List<CleaningHistory> histories =
        historyRepository.findByDatasetIdOrderByExecutedAtDesc(datasetId);
    historyRepository.deleteAll(histories);
    log.info("Deleted {} cleaning history records for dataset: {}", histories.size(), datasetId);
  }

  public long countByUser(UUID userId) {
    return historyRepository.countByUserId(userId);
  }
}
