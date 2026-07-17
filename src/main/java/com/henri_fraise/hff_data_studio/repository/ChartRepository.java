package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartRepository extends JpaRepository<Chart, UUID> {

	Optional<Chart> findByResultId(UUID resultId);

	List<Chart> findByResultIdIn(List<UUID> resultIds);

	List<Chart> findByChartType(ChartType chartType);

	List<Chart> findByResult(AnalysisResult result);

	long countByChartType(ChartType chartType);

	boolean existsByResultId(UUID resultId);

	@Modifying
	@Transactional
	void deleteByResultId(UUID resultId);

	@Modifying
	@Transactional
	void deleteByResultIdIn(List<UUID> resultIds);

	@Modifying
	@Transactional
	@Query("DELETE FROM Chart c WHERE c.result.id = :resultId")
	void deleteByResultIdQuery(@Param("resultId") UUID resultId);

	@Modifying
	@Transactional
	@Query("DELETE FROM Chart c WHERE c.result.id IN :resultIds")
	void deleteByResultIdInQuery(@Param("resultIds") List<UUID> resultIds);

	@Query("SELECT c.chartType, COUNT(c) FROM Chart c GROUP BY c.chartType")
	List<Object[]> countGroupByChartType();

	@Query("SELECT c FROM Chart c WHERE c.chartType = :chartType AND c.result.execution.id = :executionId")
	List<Chart> findByChartTypeAndExecutionId(@Param("chartType") ChartType chartType, @Param("executionId") UUID executionId);

	@Query("SELECT c FROM Chart c WHERE c.result.execution.id = :executionId")
	List<Chart> findByExecutionId(@Param("executionId") UUID executionId);

	@Query("SELECT c FROM Chart c WHERE c.result.id = :resultId AND c.chartType = :chartType")
	Optional<Chart> findByResultIdAndChartType(@Param("resultId") UUID resultId, @Param("chartType") ChartType chartType);

	@Query("SELECT c.configJson FROM Chart c WHERE c.result.id = :resultId")
	Optional<String> findConfigByResultId(@Param("resultId") UUID resultId);

	@Query("SELECT COUNT(c) FROM Chart c WHERE c.result.execution.id = :executionId")
	long countByExecutionId(@Param("executionId") UUID executionId);

	@Modifying
	@Transactional
	@Query("UPDATE Chart c SET c.chartType = :chartType WHERE c.id = :chartId")
	int updateChartType(@Param("chartId") UUID chartId, @Param("chartType") ChartType chartType);

	@Modifying
	@Transactional
	@Query("UPDATE Chart c SET c.configJson = :configJson WHERE c.id = :chartId")
	int updateConfigJson(@Param("chartId") UUID chartId, @Param("configJson") String configJson);
}