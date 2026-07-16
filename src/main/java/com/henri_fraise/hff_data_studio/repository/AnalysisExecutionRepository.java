package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, UUID> {

	Page<AnalysisExecution> findByDatasetId(UUID datasetId, Pageable pageable);

	Page<AnalysisExecution> findByUserId(UUID userId, Pageable pageable);

	Page<AnalysisExecution> findByStatus(AnalysisExecutionStatus status, Pageable pageable);

	List<AnalysisExecution> findByStatus(AnalysisExecutionStatus status);

	Page<AnalysisExecution> findByDatasetIdAndUserId(UUID datasetId, UUID userId, Pageable pageable);

	Page<AnalysisExecution> findByPredefinedAnalysisId(UUID analysisId, Pageable pageable);

	long countByDatasetId(UUID datasetId);

	long countByUserId(UUID userId);

	long countByStatus(AnalysisExecutionStatus status);

	long countByPredefinedAnalysisId(UUID analysisId);

	long countByDatasetIdAndStatus(UUID datasetId, AnalysisExecutionStatus status);


	@Modifying
	@Transactional
	@Query("UPDATE AnalysisExecution e SET e.status = :status WHERE e.id = :execution_id")
	void updateStatus(@Param("execution_id") UUID executionId, @Param("status") AnalysisExecutionStatus status);

	@Modifying
	@Transactional
	@Query("UPDATE AnalysisExecution e SET e.status = :status, e.durationMs = :duration_ms WHERE e.id = :execution_id")
	void completeExecution(@Param("execution_id") UUID executionId, @Param("status") AnalysisExecutionStatus status, @Param("duration_ms") Integer durationMs);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.status = 'IN_PROGRESS' AND e.executedAt < :timeout ")
	List<AnalysisExecution> findStalledExecutions(@Param("timeout") LocalDateTime timeout);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.dataset.id = :dataset_id AND e.status IN :statuses")
	List<AnalysisExecution> findByDatasetIdAndStatuses(@Param("dataset_id") UUID datasetId, @Param("statuses") List<AnalysisExecutionStatus> statuses);

	@Query("SELECT e FROM AnalysisExecution e WHERE e.user.id = :user_id AND " +
			"(:search_term IS NULL OR LOWER(e.dataset.datasetName) LIKE LOWER(CONCAT('%', :search_term, '%') ) OR " +
			"LOWER(e.predefinedAnalysis.analysisName) LIKE LOWER(CONCAT('%', :search_term, '%') ) )")
	Page<AnalysisExecution> searchUserExecutions(
				@Param("user_id") UUID userId,
				@Param("search_term") String searchTerm,
				Pageable pageable);

	@Query("SELECT COUNT(e) FROM AnalysisExecution  e WHERE e.executedAt BETWEEN :start_date AND :end_date")
	long countExecutionsBetween(
			@Param("start_date") LocalDateTime startDate,
			@Param("end_date") LocalDateTime endDate
	);

	@Query("SELECT AVG(e.durationMs) FROM AnalysisExecution  e WHERE  e.status = 'COMPLETED'")
	Double averageSuccessFulExecutionDuration();
}
