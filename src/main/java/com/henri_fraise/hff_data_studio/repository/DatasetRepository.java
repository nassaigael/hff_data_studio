package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Dataset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.crypto.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, UUID> {

  Page<Dataset> findBySourceFileProjectId(UUID projectId, Pageable pageable);

  Page<Dataset> findBySourceFileProjectIdAndSourceFileUserId(
      UUID projectId, UUID userId, Pageable pageable);

  Page<Dataset> findBySourceFileId(UUID fileId, Pageable pageable);

  List<Dataset> findBySourceFileId(UUID fileId);

  Page<Dataset> findByIsCleanedTrue(Pageable pageable);

  Page<Dataset> findByIsCleanedFalse(Pageable pageable);

  Optional<Dataset> findByDatasetNameAndSourceFileId(String datasetName, UUID fileId);

  Page<Dataset> findByDatasetNameContainingIgnoreCase(String datasetName, Pageable pageable);

  boolean existsByDatasetNameAndSourceFileId(String datasetName, UUID fileId);

  long countBySourceFileProjectId(UUID projectId);

  long countBySourceFileId(UUID fileId);

  long countByIsCleaned(Boolean isCleaned);

  long countBySourceFileProjectIdAndIsCleaned(UUID projectId, Boolean isCleaned);

  @Modifying
  @Transactional
  @Query("UPDATE Dataset d SET d.isCleaned = :is_cleaned WHERE d.id = :dataset_id")
  void updateIsCleaned(@Param("dataset_id") UUID datasetId, @Param("is_cleaned") Boolean isCleaned);

  @Modifying
  @Transactional
  @Query(
      "UPDATE Dataset d SET d.rowCount = :row_count, d.columnCount = :column_count WHERE d.id ="
          + " :dataset_id")
  void updateStats(
      @Param("dataset_id") UUID datasetId,
      @Param("row_count") Integer rowCount,
      @Param("column_count") Integer columnCount);

  @Query(
      "SELECT d FROM Dataset d WHERE  d.project_id = :project_id AND (:search_term IS NULL OR"
          + " LOWER(d.datasetName) LIKE LOWER(CONCAT('%', :search_term, '%') ) )")
  Page<Dataset> searchProjectDatasets(
      @Param("project_id") UUID projectId,
      @Param("search_term") String searchTerm,
      Pageable pageable);

  @Query("SELECT d FROM Dataset d WHERE d.isCleaned = false AND  d.createdAt < :date")
  List<Dataset> findUncleanedDatasetsOlderThan(@Param("date") LocalDateTime date);

  @Query("SELECT d FROM Dataset d WHERE d.project_id = :project_id ORDER BY d.createdAt DESC")
  List<Dataset> findRecentDatasetsByProjectId(@Param("project_id") UUID projectId);

  @Query("SELECT COUNT(d) FROM  Dataset d WHERE d.createdAt BETWEEN :start_date AND :end_date")
  long countDatasetsCreatedBetween(
      @Param("start_date") String startDate, @Param("end_date") String endDate);

  @Query("SELECT AVG(d.rowCount) FROM Dataset d WHERE d.project_id = :project_id")
  Double averageRowCountByProjectId(@Param("project_id") UUID projectId);
}
