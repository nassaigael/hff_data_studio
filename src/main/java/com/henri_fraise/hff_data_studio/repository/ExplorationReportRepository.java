package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExplorationReportRepository extends JpaRepository<ExplorationReport, UUID> {

	Optional<ExplorationReport> findByDatasetId(UUID datasetId);

	List<ExplorationReport> findByDatasetIdIn(List<UUID> datasetIds);

	List<ExplorationReport> findByQualityScoreGreaterThanEqual(BigDecimal score);

	Page<ExplorationReport> findByQualityScoreGreaterThanEqual(BigDecimal score, Pageable pageable);

	List<ExplorationReport> findByQualityScoreLessThanEqual(BigDecimal score);

	Page<ExplorationReport> findByQualityScoreLessThanEqual(BigDecimal score, Pageable pageable);

	List<ExplorationReport> findByQualityScoreBetween(BigDecimal minScore, BigDecimal maxScore);

	Page<ExplorationReport> findByQualityScoreBetween(BigDecimal minScore, BigDecimal maxScore, Pageable pageable);

	List<ExplorationReport> findByQualityScoreIsNull();

	long countByDatasetId(UUID datasetId);

	long countByQualityScoreGreaterThanEqual(BigDecimal score);

	long countByQualityScoreLessThanEqual(BigDecimal score);

	long countByQualityScoreBetween(BigDecimal minScore, BigDecimal maxScore);

	long countByQualityScoreIsNull();

	boolean existsByDatasetId(UUID datasetId);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.qualityScore = :score WHERE r.id = :reportId")
	void updateQualityScore(@Param("reportId") UUID reportId, @Param("score") BigDecimal score);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.qualityScore = :score WHERE r.dataset.id = :datasetId")
	void updateQualityScoreByDatasetId(@Param("datasetId") UUID datasetId, @Param("score") BigDecimal score);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.reportPdfPath = :pdfPath WHERE r.id = :reportId")
	void updateReportPdfPath(@Param("reportId") UUID reportId, @Param("pdfPath") String pdfPath);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.reportPdfPath = :pdfPath WHERE r.dataset.id = :datasetId")
	void updateReportPdfPathByDatasetId(@Param("datasetId") UUID datasetId, @Param("pdfPath") String pdfPath);

	@Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r")
	Double averageQualityScore();

	@Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r WHERE r.dataset.id = :datasetId")
	Double averageQualityScoreByDatasetId(@Param("datasetId") UUID datasetId);

	@Query("SELECT MIN(r.qualityScore) FROM ExplorationReport r")
	BigDecimal minQualityScore();

	@Query("SELECT MAX(r.qualityScore) FROM ExplorationReport r")
	BigDecimal maxQualityScore();

	@Query("SELECT SUM(r.qualityScore) FROM ExplorationReport r")
	BigDecimal sumQualityScore();

	@Query("SELECT COUNT(r) FROM ExplorationReport r")
	long countAllReports();

	@Query("SELECT COUNT(r) FROM ExplorationReport r WHERE r.qualityScore < :threshold")
	long countByQualityScoreBelowThreshold(@Param("threshold") BigDecimal threshold);

	@Query("SELECT r FROM ExplorationReport r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
	List<ExplorationReport> findByGeneratedAtBetween(@Param("startDate") LocalDateTime startDate,
	                                                 @Param("endDate") LocalDateTime endDate);

	@Query("SELECT r FROM ExplorationReport r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
	Page<ExplorationReport> findByGeneratedAtBetween(@Param("startDate") LocalDateTime startDate,
	                                                 @Param("endDate") LocalDateTime endDate,
	                                                 Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r WHERE r.generatedAt >= :startDate")
	List<ExplorationReport> findByGeneratedAtAfter(@Param("startDate") LocalDateTime startDate);

	@Query("SELECT r FROM ExplorationReport r WHERE r.generatedAt <= :endDate")
	List<ExplorationReport> findByGeneratedAtBefore(@Param("endDate") LocalDateTime endDate);

	@Query("SELECT COUNT(r) FROM ExplorationReport r WHERE r.generatedAt BETWEEN :startDate AND :endDate")
	long countByGeneratedAtBetween(@Param("startDate") LocalDateTime startDate,
	                               @Param("endDate") LocalDateTime endDate);

	@Query("SELECT r FROM ExplorationReport r ORDER BY r.qualityScore DESC")
	List<ExplorationReport> findTopByQualityScore(Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r ORDER BY r.qualityScore ASC")
	List<ExplorationReport> findBottomByQualityScore(Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r ORDER BY r.qualityScore DESC")
	Page<ExplorationReport> findAllOrderByQualityScoreDesc(Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r ORDER BY r.qualityScore ASC")
	Page<ExplorationReport> findAllOrderByQualityScoreAsc(Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r ORDER BY r.generatedAt DESC")
	List<ExplorationReport> findRecentReports(Pageable pageable);

	@Query("SELECT " +
			"CASE " +
			"WHEN r.qualityScore >= 90 THEN '90-100' " +
			"WHEN r.qualityScore >= 75 THEN '75-89' " +
			"WHEN r.qualityScore >= 50 THEN '50-74' " +
			"WHEN r.qualityScore >= 25 THEN '25-49' " +
			"ELSE '0-24' END AS range, " +
			"COUNT(r) " +
			"FROM ExplorationReport r " +
			"GROUP BY range " +
			"ORDER BY range DESC")
	List<Object[]> getQualityScoreDistribution();

	@Query("SELECT r.qualityScore, COUNT(r) FROM ExplorationReport r GROUP BY r.qualityScore ORDER BY r.qualityScore")
	List<Object[]> getQualityScoreFrequency();

	@Query("SELECT r FROM ExplorationReport r JOIN FETCH r.dataset WHERE r.id = :reportId")
	Optional<ExplorationReport> findByIdWithDataset(@Param("reportId") UUID reportId);

	@Query("SELECT r FROM ExplorationReport r JOIN FETCH r.dataset WHERE r.dataset.id = :datasetId")
	Optional<ExplorationReport> findByDatasetIdWithDataset(@Param("datasetId") UUID datasetId);

	@Query("SELECT r FROM ExplorationReport r JOIN FETCH r.dataset ORDER BY r.qualityScore DESC")
	List<ExplorationReport> findAllWithDatasetOrderByQualityScoreDesc(Pageable pageable);

	@Query("SELECT r FROM ExplorationReport r JOIN FETCH r.dataset WHERE r.qualityScore >= :minScore ORDER BY r.qualityScore DESC")
	List<ExplorationReport> findHighQualityReportsWithDataset(@Param("minScore") BigDecimal minScore, Pageable pageable);

	@Modifying
	@Transactional
	@Query("DELETE FROM ExplorationReport r WHERE r.dataset.id = :datasetId")
	void deleteByDatasetId(@Param("datasetId") UUID datasetId);

	@Modifying
	@Transactional
	@Query("DELETE FROM ExplorationReport r WHERE r.generatedAt < :date")
	void deleteByGeneratedAtBefore(@Param("date") LocalDateTime date);

	@Modifying
	@Transactional
	@Query("DELETE FROM ExplorationReport r WHERE r.qualityScore < :threshold")
	void deleteByQualityScoreLessThan(@Param("threshold") BigDecimal threshold);

	@Query("SELECT r.id, r.dataset.id, r.dataset.datasetName, r.qualityScore, r.generatedAt " +
			"FROM ExplorationReport r ORDER BY r.qualityScore DESC")
	List<Object[]> findTopQualityReports(@Param("limit") int limit);

	@Query("SELECT r.id, r.dataset.id, r.dataset.datasetName, r.qualityScore, r.generatedAt " +
			"FROM ExplorationReport r ORDER BY r.qualityScore ASC")
	List<Object[]> findBottomQualityReports(@Param("limit") int limit);

	@Query("SELECT r.dataset.id, r.dataset.datasetName, r.qualityScore " +
			"FROM ExplorationReport r WHERE r.dataset.id IN :datasetIds")
	List<Object[]> findQualityScoresByDatasetIds(@Param("datasetIds") List<UUID> datasetIds);

	@Query("SELECT r.dataset.id, r.dataset.datasetName, r.qualityScore " +
			"FROM ExplorationReport r WHERE r.dataset.sourceFile.project.id = :projectId " +
			"ORDER BY r.qualityScore DESC")
	List<Object[]> findQualityScoresByProjectId(@Param("projectId") UUID projectId);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.qualityScore = :score WHERE r.id IN :reportIds")
	void updateQualityScoreBulk(@Param("reportIds") List<UUID> reportIds, @Param("score") BigDecimal score);

	@Modifying
	@Transactional
	@Query("UPDATE ExplorationReport r SET r.qualityScore = :score WHERE r.dataset.id IN :datasetIds")
	void updateQualityScoreByDatasetIds(@Param("datasetIds") List<UUID> datasetIds, @Param("score") BigDecimal score);
}