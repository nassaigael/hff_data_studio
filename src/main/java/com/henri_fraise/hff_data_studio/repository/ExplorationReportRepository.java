package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExplorationReportRepository extends JpaRepository<ExplorationReport, UUID> {

	Optional<ExplorationReport> findByDatasetId(UUID datasetId);

	List<ExplorationReport> findByQualityScoreGreaterThanEqual(BigDecimal score);

	List<ExplorationReport> findByQualityScoreLessThan(BigDecimal score);

	long countByQualityScoreGreaterThanEqual(BigDecimal score);

	long countByQualityScoreLessThan(BigDecimal score);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport  r SET r.qualityScore = :score WHERE r.id = :report_id")
	void updateQualityScore(
			@Param("report_id") UUID reportId, @Param("score") BigDecimal score);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.reportPdfPath = :pdf_path WHERE r.id = :report_id")
	void updateReportPdfPath(
			@Param("report_id") UUID reportId, @Param("pdf_path") String pdfPath);

	@Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r")
	BigDecimal averageQualityScore();

	@Query("SELECT r FROM ExplorationReport  r WHERE r.generatedAt BETWEEN :start_date AND :end_date")
	List<ExplorationReport> findByGeneratedDateRange(
			@Param("start_date") String startDate, @Param("end_date") String endDate);

	@Query("SELECT  r FROM ExplorationReport  r ORDER BY r.qualityScore DESC")
	List<ExplorationReport> findTopByQualityScore(Pageable pageable);

	@Query("SELECT COUNT(r) FROM ExplorationReport  r WHERE  r.qualityScore < :threshold")
	long countByQualityScoreBelowThreshold(@Param("threshold") BigDecimal threshold);
}
