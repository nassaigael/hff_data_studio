package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartRepository  extends JpaRepository<Chart, UUID> {
	
	Optional<Chart> findByResultId(UUID resultId);

	List<Chart> findByChartType(ChartType chartType);

	long countByChartType(ChartType chartType);

	@Query("SELECT c FROM Chart c WHERE c.result.execution.id = :execution_id")
	List<Chart> findByExecutionId(@Param("execution_id") UUID executionId);

	@Query("SELECT  c FROM Chart c WHERE c.result.execution.id = :execution_id AND c.chartType = :chart_type")
	List<Chart> findByExecutionIdAndChartType(@Param("execution_id") UUID executionId, @Param("chart_type") ChartType chartType);

	@Query("SELECT  COUNT(c) FROM Chart c WHERE c.chartType = :chart_type AND c.result.execution.id = :execution_id")
	long countByExecutionIdAndChartType(@Param("execution_id") UUID executionId, @Param("chart_type") ChartType chartType);

	@Query("SELECT DISTINCT c.chartType FROM Chart c")
	List<Chart> findAllDistinctChartTypes();

}
