package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
