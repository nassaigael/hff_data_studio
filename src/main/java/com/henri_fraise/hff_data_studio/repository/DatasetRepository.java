package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

  // ==================== FIND BY PROJECT ====================

  Page<Dataset> findBySourceFileProjectId(UUID projectId, Pageable pageable);

  List<Dataset> findBySourceFileProjectIdOrderByCreatedAtDesc(UUID projectId);

  List<Dataset> findBySourceFileProjectIdOrderByCreatedAtAsc(UUID projectId);

  List<Dataset> findBySourceFileProjectIdAndIsCleanedTrue(UUID projectId);

  List<Dataset> findBySourceFileProjectIdAndIsCleanedFalse(UUID projectId);

  Page<Dataset> findBySourceFileProjectIdAndIsCleanedTrue(UUID projectId, Pageable pageable);

  Page<Dataset> findBySourceFileProjectIdAndIsCleanedFalse(UUID projectId, Pageable pageable);

  // ==================== FIND BY FILE ====================

  Page<Dataset> findBySourceFileId(UUID fileId, Pageable pageable);

  List<Dataset> findBySourceFileIdOrderByCreatedAtAsc(UUID fileId);

  List<Dataset> findBySourceFileIdOrderByCreatedAtDesc(UUID fileId);

  List<Dataset> findBySourceFileIdAndIsCleanedTrue(UUID fileId);

  List<Dataset> findBySourceFileIdAndIsCleanedFalse(UUID fileId);

  Optional<Dataset> findBySourceFileIdAndDatasetName(UUID fileId, String datasetName);

  // ==================== FIND BY NAME ====================

  List<Dataset> findByDatasetNameContainingIgnoreCase(String searchTerm);

  Page<Dataset> findByDatasetNameContainingIgnoreCase(String searchTerm, Pageable pageable);

  Optional<Dataset> findByDatasetName(String datasetName);

  List<Dataset> findByDatasetNameStartingWithIgnoreCase(String prefix);

  // ==================== FIND BY DATE ====================

  List<Dataset> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  Page<Dataset> findByCreatedAtBetween(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  List<Dataset> findByCreatedAtBefore(LocalDateTime date);

  List<Dataset> findByCreatedAtAfter(LocalDateTime date);

  List<Dataset> findByCreatedAtBetweenAndIsCleanedTrue(
      LocalDateTime startDate, LocalDateTime endDate);

  List<Dataset> findByCreatedAtBetweenAndIsCleanedFalse(
      LocalDateTime startDate, LocalDateTime endDate);

  // ==================== FIND BY CLEANED STATUS ====================

  List<Dataset> findByIsCleanedTrue();

  Page<Dataset> findByIsCleanedTrue(Pageable pageable);

  List<Dataset> findByIsCleanedFalse();

  Page<Dataset> findByIsCleanedFalse(Pageable pageable);

  // ==================== COUNT METHODS ====================

  long countBySourceFileProjectId(UUID projectId);

  long countBySourceFileId(UUID fileId);

  long countByIsCleanedTrue();

  long countByIsCleanedFalse();

  long countBySourceFileProjectIdAndIsCleanedTrue(UUID projectId);

  long countBySourceFileProjectIdAndIsCleanedFalse(UUID projectId);

  long countBySourceFileIdAndIsCleanedTrue(UUID fileId);

  long countBySourceFileIdAndIsCleanedFalse(UUID fileId);

  long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  long countByCreatedAtBefore(LocalDateTime date);

  long countByCreatedAtAfter(LocalDateTime date);

  // ==================== EXISTS METHODS ====================

  boolean existsByDatasetNameAndSourceFileId(String datasetName, UUID fileId);

  boolean existsByDatasetName(String datasetName);

  boolean existsBySourceFileProjectId(UUID projectId);

  boolean existsBySourceFileId(UUID fileId);

  boolean existsByIsCleanedTrue();

  // ==================== SUM / AVG METHODS ====================

  @Query("SELECT SUM(d.rowCount) FROM Dataset d")
  Long sumRowCount();

  @Query("SELECT SUM(d.rowCount) FROM Dataset d WHERE d.sourceFile.project.id = :projectId")
  Long sumRowCountByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT SUM(d.rowCount) FROM Dataset d WHERE d.sourceFile.id = :fileId")
  Long sumRowCountByFileId(@Param("fileId") UUID fileId);

  @Query("SELECT SUM(d.rowCount) FROM Dataset d WHERE d.isCleaned = true")
  Long sumRowCountCleaned();

  @Query("SELECT SUM(d.rowCount) FROM Dataset d WHERE d.isCleaned = false")
  Long sumRowCountUncleaned();

  @Query("SELECT SUM(d.columnCount) FROM Dataset d")
  Long sumColumnCount();

  @Query("SELECT SUM(d.columnCount) FROM Dataset d WHERE d.sourceFile.project.id = :projectId")
  Long sumColumnCountByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT SUM(d.columnCount) FROM Dataset d WHERE d.sourceFile.id = :fileId")
  Long sumColumnCountByFileId(@Param("fileId") UUID fileId);

  @Query("SELECT SUM(d.columnCount) FROM Dataset d WHERE d.isCleaned = true")
  Long sumColumnCountCleaned();

  @Query("SELECT SUM(d.columnCount) FROM Dataset d WHERE d.isCleaned = false")
  Long sumColumnCountUncleaned();

  @Query("SELECT AVG(d.rowCount) FROM Dataset d")
  Double averageRowCount();

  @Query("SELECT AVG(d.rowCount) FROM Dataset d WHERE d.sourceFile.project.id = :projectId")
  Double averageRowCountByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT AVG(d.rowCount) FROM Dataset d WHERE d.isCleaned = true")
  Double averageRowCountCleaned();

  @Query("SELECT AVG(d.rowCount) FROM Dataset d WHERE d.isCleaned = false")
  Double averageRowCountUncleaned();

  @Query("SELECT AVG(d.columnCount) FROM Dataset d")
  Double averageColumnCount();

  @Query("SELECT AVG(d.columnCount) FROM Dataset d WHERE d.sourceFile.project.id = :projectId")
  Double averageColumnCountByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT AVG(d.columnCount) FROM Dataset d WHERE d.isCleaned = true")
  Double averageColumnCountCleaned();

  @Query("SELECT AVG(d.columnCount) FROM Dataset d WHERE d.isCleaned = false")
  Double averageColumnCountUncleaned();

  // ==================== SEARCH METHODS ====================

  @Query(
      "SELECT d FROM Dataset d WHERE d.sourceFile.project.id = :projectId "
          + "AND (LOWER(d.datasetName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Dataset> searchProjectDatasets(
      @Param("projectId") UUID projectId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query(
      "SELECT d FROM Dataset d WHERE "
          + "LOWER(d.datasetName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(d.sourceFile.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<Dataset> searchAllDatasets(@Param("searchTerm") String searchTerm);

  @Query(
      "SELECT d FROM Dataset d WHERE "
          + "LOWER(d.datasetName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(d.sourceFile.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Dataset> searchAllDatasets(@Param("searchTerm") String searchTerm, Pageable pageable);

  // ==================== RECENT METHODS ====================

  @Query("SELECT d FROM Dataset d ORDER BY d.createdAt DESC")
  List<Dataset> findRecentDatasets(@Param("limit") int limit);

  @Query(value = "SELECT * FROM dataset ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
  List<Dataset> findRecentDatasetsNative(@Param("limit") int limit);

  @Query(
      "SELECT d FROM Dataset d WHERE d.sourceFile.project.id = :projectId ORDER BY d.createdAt"
          + " DESC")
  List<Dataset> findRecentDatasetsByProjectId(
      @Param("projectId") UUID projectId, @Param("limit") int limit);

  // ==================== UNCLEANED / OLD METHODS ====================

  @Query("SELECT d FROM Dataset d WHERE d.isCleaned = false AND d.createdAt < :threshold")
  List<Dataset> findUncleanedDatasetsOlderThan(@Param("threshold") LocalDateTime threshold);

  @Query("SELECT d FROM Dataset d WHERE d.isCleaned = false AND d.createdAt < :threshold")
  Page<Dataset> findUncleanedDatasetsOlderThan(
      @Param("threshold") LocalDateTime threshold, Pageable pageable);

  @Query("SELECT d FROM Dataset d WHERE d.isCleaned = true AND d.createdAt < :threshold")
  List<Dataset> findCleanedDatasetsOlderThan(@Param("threshold") LocalDateTime threshold);

  // ==================== GROUP BY / STATISTICS METHODS ====================

  @Query("SELECT d.sourceFile.project.id, COUNT(d) FROM Dataset d GROUP BY d.sourceFile.project.id")
  List<Object[]> countGroupByProject();

  @Query("SELECT d.sourceFile.id, COUNT(d) FROM Dataset d GROUP BY d.sourceFile.id")
  List<Object[]> countGroupByFile();

  @Query("SELECT d.isCleaned, COUNT(d) FROM Dataset d GROUP BY d.isCleaned")
  List<Object[]> countGroupByCleanedStatus();

  @Query(
      "SELECT YEAR(d.createdAt), MONTH(d.createdAt), COUNT(d) FROM Dataset d GROUP BY"
          + " YEAR(d.createdAt), MONTH(d.createdAt) ORDER BY YEAR(d.createdAt) DESC,"
          + " MONTH(d.createdAt) DESC")
  List<Object[]> countGroupByYearMonth();

  @Query(
      "SELECT DATE(d.createdAt), COUNT(d) FROM Dataset d GROUP BY DATE(d.createdAt) ORDER BY"
          + " DATE(d.createdAt) DESC")
  List<Object[]> countGroupByDate();

  // ==================== QUALITY SCORE METHODS ====================

  @Query(
      "SELECT AVG(r.qualityScore) FROM ExplorationReport r WHERE r.dataset.id IN (SELECT d.id FROM"
          + " Dataset d WHERE d.sourceFile.project.id = :projectId)")
  Double averageQualityScoreByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r WHERE r.dataset.isCleaned = true")
  Double averageQualityScoreCleaned();

  @Query("SELECT AVG(r.qualityScore) FROM ExplorationReport r WHERE r.dataset.isCleaned = false")
  Double averageQualityScoreUncleaned();

  // ==================== BATCH / DELETE METHODS ====================

  @Query("DELETE FROM Dataset d WHERE d.sourceFile.id = :fileId")
  void deleteBySourceFileId(@Param("fileId") UUID fileId);

  @Query("DELETE FROM Dataset d WHERE d.sourceFile.project.id = :projectId")
  void deleteByProjectId(@Param("projectId") UUID projectId);

  void deleteByCreatedAtBefore(LocalDateTime date);

  // ==================== CUSTOM QUERY METHODS ====================

  @Query("SELECT d FROM Dataset d WHERE d.rowCount > :minRows AND d.columnCount > :minColumns")
  List<Dataset> findDatasetsWithMinimumDimensions(
      @Param("minRows") int minRows, @Param("minColumns") int minColumns);

  @Query("SELECT d FROM Dataset d WHERE d.rowCount < :maxRows")
  List<Dataset> findDatasetsWithMaxRows(@Param("maxRows") int maxRows);

  @Query("SELECT d FROM Dataset d WHERE d.columnCount > :minColumns")
  List<Dataset> findDatasetsWithMinColumns(@Param("minColumns") int minColumns);

  @Query("SELECT d FROM Dataset d WHERE d.datasetName LIKE CONCAT('%', :suffix)")
  List<Dataset> findDatasetsByNameSuffix(@Param("suffix") String suffix);

  @Query("SELECT d FROM Dataset d WHERE LENGTH(d.datasetName) > :minLength")
  List<Dataset> findDatasetsByNameLengthGreaterThan(@Param("minLength") int minLength);

  // ==================== DISTINCT METHODS ====================

  @Query("SELECT DISTINCT d.sourceFile.project.id FROM Dataset d")
  List<UUID> findDistinctProjectIds();

  @Query("SELECT DISTINCT d.sourceFile.id FROM Dataset d")
  List<UUID> findDistinctFileIds();

  @Query("SELECT DISTINCT d.datasetName FROM Dataset d")
  List<String> findDistinctDatasetNames();
}
