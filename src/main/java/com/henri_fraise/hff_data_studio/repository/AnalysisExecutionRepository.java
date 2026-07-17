package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, UUID> {

	List<AnalysisExecution> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId);

	Page<AnalysisExecution> findByDatasetIdOrderByExecutedAtDesc(UUID datasetId, Pageable pageable);

	List<AnalysisExecution> findByUserIdOrderByExecutedAtDesc(UUID userId);

	List<AnalysisExecution> findByStatus(AnalysisExecutionStatus status);

	Page<AnalysisExecution> findByStatus(AnalysisExecutionStatus status, Pageable pageable);

	List<AnalysisExecution> findByExecutedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	long countByDatasetId(UUID datasetId);

	long countByUserId(UUID userId);

	long countByStatus(AnalysisExecutionStatus status);

	long countByPredefinedAnalysisId(UUID analysisId);

	long countByExecutedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	@Query("SELECT AVG(e.durationMs) FROM AnalysisExecution e WHERE e.status = 'COMPLETED'")
	Double averageDuration();

	@Query("SELECT AVG(e.durationMs) FROM AnalysisExecution e WHERE e.predefinedAnalysis.id = :analysisId AND e.status = 'COMPLETED'")
	Double averageDurationByAnalysisId(@Param("analysisId") UUID analysisId);

	@Query("SELECT AVG(e.durationMs) FROM AnalysisExecution e WHERE e.user.id = :userId AND e.status = 'COMPLETED'")
	Double averageDurationByUserId(@Param("userId") UUID userId);

	@Query("SELECT AVG(e.durationMs) FROM AnalysisExecution e WHERE e.dataset.id = :datasetId AND e.status = 'COMPLETED'")
	Double averageDurationByDatasetId(@Param("datasetId") UUID datasetId);

	@Query("SELECT e FROM AnalysisExecution e ORDER BY e.executedAt DESC")
	List<AnalysisExecution> findRecentExecutions(Pageable pageable);

	@Query("SELECT e.status, COUNT(e) FROM AnalysisExecution e GROUP BY e.status")
	List<Object[]> countGroupByStatus();

	@Query("SELECT e.user.id, COUNT(e) FROM AnalysisExecution e GROUP BY e.user.id")
	List<Object[]> countGroupByUser();

	@Query("SELECT e.dataset.id, COUNT(e) FROM AnalysisExecution e GROUP BY e.dataset.id")
	List<Object[]> countGroupByDataset();

	@Query("SELECT e FROM AnalysisExecution e WHERE e.status = :status AND e.executedAt < :date")
	List<AnalysisExecution> findOldExecutionsByStatus(@Param("status") AnalysisExecutionStatus status, @Param("date") LocalDateTime date);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.predefinedAnalysis.id = :analysisId ORDER BY e.executedAt DESC")
	List<AnalysisExecution> findByPredefinedAnalysisId(@Param("analysisId") UUID analysisId);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.dataset.id = :datasetId AND e.status = :status")
	List<AnalysisExecution> findByDatasetIdAndStatus(@Param("datasetId") UUID datasetId, @Param("status") AnalysisExecutionStatus status);

	@Query("SELECT COUNT(e) FROM AnalysisExecution e WHERE e.dataset.id = :datasetId AND e.status = :status")
	long countByDatasetIdAndStatus(@Param("datasetId") UUID datasetId, @Param("status") AnalysisExecutionStatus status);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.user.id = :userId AND e.status = :status")
	List<AnalysisExecution> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") AnalysisExecutionStatus status);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.status = 'IN_PROGRESS' AND e.executedAt < :timeout")
	List<AnalysisExecution> findStuckExecutions(@Param("timeout") LocalDateTime timeout);

	@Query("UPDATE AnalysisExecution e SET e.status = 'ERROR' WHERE e.id = :executionId")
	void markAsError(@Param("executionId") UUID executionId);

	@Query("SELECT COUNT(e) FROM AnalysisExecution e WHERE e.predefinedAnalysis.id IS NOT NULL")
	long countPredefinedAnalyses();

	@Query("SELECT COUNT(e) FROM AnalysisExecution e WHERE e.predefinedAnalysis.id IS NULL")
	long countCustomAnalyses();
}