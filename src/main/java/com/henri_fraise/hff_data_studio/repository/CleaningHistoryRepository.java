package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CleaningHistoryRepository extends JpaRepository<CleaningHistory, UUID> {

	Page<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId, Pageable pageable);

	List<CleaningHistory> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId);

	Page<CleaningHistory> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);

	List<CleaningHistory> findByUserIdOrderByExecutedAtDesc(UUID userId);

	List<CleaningHistory> findByStatus(CleaningHistoryStatus status);

	Page<CleaningHistory> findByStatus(CleaningHistoryStatus status, Pageable pageable);

	long countByDatasetId(UUID datasetId);

	long countByStatus(CleaningHistoryStatus status);

	long countByDatasetIdAndStatus(UUID datasetId, CleaningHistoryStatus status);

	long countByUserIdAndStatus(UUID userId, CleaningHistoryStatus status);

	@Query("SELECT h from  CleaningHistory h WHERE  h.dataset.id = :dataset_id  AND h.executedAt >= :since")
	List<CleaningHistory> findByDatasetIdSince(UUID datasetId, String since);

	@Query("SELECT h FROM CleaningHistory h WHERE h.executedAt BETWEEN :start_date AND :end_date")
	List<CleaningHistory> findByExecutedDateRange(LocalDateTime start_date, LocalDateTime end_date);

	@Query("SELECT h FROM CleaningHistory h WHERE h.status = :status AND h.executedAt < :date")
	List<CleaningHistory> findOldRecordsByStatus(CleaningHistoryStatus status, LocalDateTime date);

	@Query("SELECT COUNT(h) FROM CleaningHistory h WHERE h.executedAt BETWEEN :start_date AND :end_date")
	long countCleaningOperationBetween(LocalDateTime start_date, LocalDateTime end_date);

	@Query("SELECT AVG(h.durationMs) FROM CleaningHistory h WHERE h.status = 'SUCCESS' ")
	Double averageSuccessfulCleaningDuration();



}
