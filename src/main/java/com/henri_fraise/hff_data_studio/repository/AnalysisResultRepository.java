package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {

	List<AnalysisResult> findByExecutionIdOrderByDisplayOrderAsc(UUID executionId);

	List<AnalysisResult> findByExecutionIdAndResultType(UUID executionId, ResultType resultType);

	List<AnalysisResult> findByExecutionIdAndResultTypeIn(UUID executionId, List<ResultType> resultTypes);

}
