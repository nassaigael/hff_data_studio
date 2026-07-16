package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

	List<AnalysisResult> findByExecutionIdOrderByDisplayOrderAsc(UUID executionId);

	List<AnalysisResult> findByExecutionIdAndResultType(UUID executionId, ResultType resultType);

	List<AnalysisResult> findByExecutionIdAndResultTypeIn(UUID executionId, List<ResultType> resultTypes);

	long countByExecutionId(UUID executionId);

	long countByExecutionIdAndResultType(UUID executionId, ResultType resultType);

	@Query("SELECT r FROM AnalysisResult r WHERE r.execution.id = :execution_id AND r.resultType = 'CHART'")
	List<AnalysisResult> findChartByExecutionId(@Param("execution_id") UUID executionId);

	@Query("SELECT r FROM AnalysisResult r WHERE r.execution.id = :execution_id AND r.resultType = 'TABLE'")
	List<AnalysisResult> findTableByExecutionId(@Param("execution_id") UUID executionId);

	@Query("SELECT r FROM AnalysisResult  r WHERE r.execution.id = :execution_id AND r.resultType = 'KPI'")
	List<AnalysisResult> findKPIByExecutionId(@Param("execution_id") UUID executionId);

	@Query("SELECT r FROM AnalysisResult r WHERE r.fileFormat = :file_format AND r.execution.id = :execution_id")
	List<AnalysisResult> findByExecutionIdAndFileFormat(@Param("execution_id") UUID executionId, @Param("file_format") String fileFormat);
	
}
