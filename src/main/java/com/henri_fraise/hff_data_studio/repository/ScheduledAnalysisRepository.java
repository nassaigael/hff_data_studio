package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ScheduledAnalysis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledAnalysisRepository extends JpaRepository<ScheduledAnalysis, UUID> {

	List<ScheduledAnalysis> findByUserIdOrderByCreatedAtDesc(UUID userId);

	List<ScheduledAnalysis> findByIsActiveTrue();

	@Query("SELECT s FROM ScheduledAnalysis s WHERE s.isActive = true " +
			"AND s.nextRunAt <= :now")
	List<ScheduledAnalysis> findDueSchedules(@Param("now") LocalDateTime now);

	@Query("SELECT s FROM ScheduledAnalysis s WHERE s.dataset.id = :datasetId")
	List<ScheduledAnalysis> findByDatasetId(@Param("datasetId") UUID datasetId);
}