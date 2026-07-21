package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CleaningHistoryRepository extends JpaRepository<CleaningHistory, UUID> {

  Page<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId, Pageable pageable);

  List<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId);

  List<CleaningHistory> findByDatasetIdAndStatus(UUID datasetId, CleaningHistoryStatus status);

  long countByDatasetId(UUID datasetId);

  long countByDatasetIdAndStatus(UUID datasetId, CleaningHistoryStatus status);

  @Query(
      "SELECT h FROM CleaningHistory h WHERE h.dataset.id = :datasetId AND h.executedAt >= :since")
  List<CleaningHistory> findByDatasetIdSince(
      @Param("datasetId") UUID datasetId, @Param("since") LocalDateTime since);

  Page<CleaningHistory> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);

  List<CleaningHistory> findByUserIdOrderByExecutedAtDesc(UUID userId);

  long countByUserIdAndStatus(UUID userId, CleaningHistoryStatus status);

  List<CleaningHistory> findByStatus(CleaningHistoryStatus status);

  Page<CleaningHistory> findByStatus(CleaningHistoryStatus status, Pageable pageable);

  long countByStatus(CleaningHistoryStatus status);

  @Query("SELECT h FROM CleaningHistory h WHERE h.executedAt BETWEEN :startDate AND :endDate")
  List<CleaningHistory> findByExecutedDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT COUNT(h) FROM CleaningHistory h WHERE h.executedAt BETWEEN :startDate AND :endDate")
  long countCleaningOperationBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT h FROM CleaningHistory h WHERE h.status = :status AND h.executedAt < :date")
  List<CleaningHistory> findOldRecordsByStatus(
      @Param("status") CleaningHistoryStatus status, @Param("date") LocalDateTime date);

  @Query("SELECT AVG(h.durationMs) FROM CleaningHistory h WHERE h.status = 'SUCCESS'")
  Double averageSuccessfulCleaningDuration();

  @Query("SELECT COUNT(h) FROM CleaningHistory h WHERE h.status = 'FAILURE'")
  long countFailures();

  @Query("SELECT COUNT(h) FROM CleaningHistory h WHERE h.status = 'PARTIAL_SUCCESS'")
  long countPartialSuccess();

  @Query(
      "SELECT h.dataset.id, COUNT(h) FROM CleaningHistory h GROUP BY h.dataset.id ORDER BY COUNT(h)"
          + " DESC")
  List<Object[]> countByDatasetGrouped();

  @Query(
      "SELECT h.user.id, COUNT(h) FROM CleaningHistory h GROUP BY h.user.id ORDER BY COUNT(h) DESC")
  List<Object[]> countByUserGrouped();

  @Query(
      "SELECT h FROM CleaningHistory h WHERE h.dataset.id = :datasetId ORDER BY h.executedAt DESC")
  List<CleaningHistory> findLatestByDatasetId(
      @Param("datasetId") UUID datasetId, Pageable pageable);

  @Query("SELECT h FROM CleaningHistory h WHERE h.status = :status ORDER BY h.executedAt DESC")
  List<CleaningHistory> findLatestByStatus(
      @Param("status") CleaningHistoryStatus status, Pageable pageable);

  @Query(
      "SELECT "
          + "CAST(SUM(CASE WHEN h.status = 'SUCCESS' THEN 1 ELSE 0 END) AS DOUBLE) / "
          + "CAST(COUNT(h) AS DOUBLE) * 100 "
          + "FROM CleaningHistory h "
          + "WHERE h.dataset.id = :datasetId")
  Double calculateSuccessRateForDataset(@Param("datasetId") UUID datasetId);

  @Query(
      "SELECT "
          + "CAST(SUM(CASE WHEN h.status = 'SUCCESS' THEN 1 ELSE 0 END) AS DOUBLE) / "
          + "CAST(COUNT(h) AS DOUBLE) * 100 "
          + "FROM CleaningHistory h")
  Double calculateOverallSuccessRate();

  @Query("SELECT COUNT(h) FROM CleaningHistory h WHERE h.rule.column.id = :columnId")
  long countByColumnId(@Param("columnId") UUID columnId);

  long countByUserId(UUID userId);
}
