package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class CustomDatasetRepositoryImpl implements CustomDatasetRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public DatasetStatisticsResponse getDatasetStatistics() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT COUNT(*) as total_datasets, SUM(row_count) as total_rows, SUM(column_count)"
                  + " as total_columns, SUM(CASE WHEN is_cleaned = true THEN 1 ELSE 0 END) as"
                  + " cleaned_datasets, SUM(CASE WHEN is_cleaned = false THEN 1 ELSE 0 END) as"
                  + " uncleaned_datasets, AVG(er.quality_score) as avg_quality_score, (SELECT"
                  + " COUNT(*) FROM dataset WHERE created_at >= NOW() - INTERVAL '30 days') as"
                  + " new_datasets_last_30_days FROM dataset d LEFT JOIN exploration_report er ON"
                  + " d.dataset_id = er.dataset_id");

      Object[] result = (Object[]) query.getSingleResult();

      return DatasetStatisticsResponse.builder()
          .totalDatasets(getLongValue(result[0]))
          .totalRows(getLongValue(result[1]))
          .totalColumns(getLongValue(result[2]))
          .cleanedDatasets(getLongValue(result[3]))
          .uncleanedDatasets(getLongValue(result[4]))
          .averageQualityScore(getDoubleValue(result[5]))
          .newDatasetsLast30Days(getLongValue(result[6]))
          .build();

    } catch (Exception e) {
      log.error("Error getting dataset statistics: {}", e.getMessage(), e);
      return DatasetStatisticsResponse.builder()
          .totalDatasets(0L)
          .totalRows(0L)
          .totalColumns(0L)
          .cleanedDatasets(0L)
          .uncleanedDatasets(0L)
          .averageQualityScore(0.0)
          .newDatasetsLast30Days(0L)
          .build();
    }
  }

  @Override
  public Map<String, Object> getDatasetStatisticsMap() {
    Map<String, Object> stats = new HashMap<>();
    try {
      DatasetStatisticsResponse response = getDatasetStatistics();
      stats.put("totalDatasets", response.getTotalDatasets());
      stats.put("totalRows", response.getTotalRows());
      stats.put("totalColumns", response.getTotalColumns());
      stats.put("cleanedDatasets", response.getCleanedDatasets());
      stats.put("uncleanedDatasets", response.getUncleanedDatasets());
      stats.put("averageQualityScore", response.getAverageQualityScore());
      stats.put("newDatasetsLast30Days", response.getNewDatasetsLast30Days());

      Query projectQuery =
          entityManager.createNativeQuery(
              "SELECT COUNT(DISTINCT sf.project_id) FROM dataset d JOIN source_file sf ON"
                  + " d.source_file_id = sf.file_id");
      stats.put("distinctProjects", getLongValue(projectQuery.getSingleResult()));

      Query fileQuery =
          entityManager.createNativeQuery("SELECT COUNT(DISTINCT source_file_id) FROM dataset");
      stats.put("distinctFiles", getLongValue(fileQuery.getSingleResult()));

    } catch (Exception e) {
      log.error("Error getting dataset statistics map: {}", e.getMessage(), e);
    }
    return stats;
  }

  @Override
  public long countDatasetsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId");
      query.setParameter("projectId", projectId);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting datasets by project: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countCleanedDatasetsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId AND"
                  + " d.isCleaned = true");
      query.setParameter("projectId", projectId);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting cleaned datasets by project: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countUncleanedDatasetsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId AND"
                  + " d.isCleaned = false");
      query.setParameter("projectId", projectId);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting uncleaned datasets by project: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countDatasetsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(d) FROM Dataset d WHERE d.createdAt BETWEEN :startDate AND :endDate");
      query.setParameter("startDate", startDate);
      query.setParameter("endDate", endDate);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting datasets created between dates: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countDatasetsCreatedAfter(LocalDateTime date) {
    try {
      Query query =
          entityManager.createQuery("SELECT COUNT(d) FROM Dataset d WHERE d.createdAt > :date");
      query.setParameter("date", date);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting datasets created after date: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countDatasetsCreatedBefore(LocalDateTime date) {
    try {
      Query query =
          entityManager.createQuery("SELECT COUNT(d) FROM Dataset d WHERE d.createdAt < :date");
      query.setParameter("date", date);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting datasets created before date: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public double getAverageDatasetQualityScore() {
    try {
      Query query =
          entityManager.createQuery("SELECT AVG(r.qualityScore) FROM ExplorationReport r");
      Object result = query.getSingleResult();
      return getDoubleValue(result);
    } catch (Exception e) {
      log.error("Error getting average dataset quality score: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public double getAverageDatasetQualityScoreByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT AVG(r.qualityScore) FROM ExplorationReport r WHERE"
                  + " r.dataset.sourceFile.project.id = :projectId");
      query.setParameter("projectId", projectId);
      Object result = query.getSingleResult();
      return getDoubleValue(result);
    } catch (Exception e) {
      log.error("Error getting average dataset quality score by project: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public long getTotalRows() {
    try {
      Query query = entityManager.createQuery("SELECT SUM(d.rowCount) FROM Dataset d");
      Object result = query.getSingleResult();
      return getLongValue(result);
    } catch (Exception e) {
      log.error("Error getting total rows: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long getTotalRowsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT SUM(d.rowCount) FROM Dataset d WHERE d.sourceFile.project.id = :projectId");
      query.setParameter("projectId", projectId);
      Object result = query.getSingleResult();
      return getLongValue(result);
    } catch (Exception e) {
      log.error("Error getting total rows by project: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long getTotalColumns() {
    try {
      Query query = entityManager.createQuery("SELECT SUM(d.columnCount) FROM Dataset d");
      Object result = query.getSingleResult();
      return getLongValue(result);
    } catch (Exception e) {
      log.error("Error getting total columns: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long getTotalColumnsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT SUM(d.columnCount) FROM Dataset d WHERE d.sourceFile.project.id ="
                  + " :projectId");
      query.setParameter("projectId", projectId);
      Object result = query.getSingleResult();
      return getLongValue(result);
    } catch (Exception e) {
      log.error("Error getting total columns by project: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public double getAverageRowsPerDataset() {
    try {
      Query query = entityManager.createQuery("SELECT AVG(d.rowCount) FROM Dataset d");
      Object result = query.getSingleResult();
      return getDoubleValue(result);
    } catch (Exception e) {
      log.error("Error getting average rows per dataset: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public double getAverageColumnsPerDataset() {
    try {
      Query query = entityManager.createQuery("SELECT AVG(d.columnCount) FROM Dataset d");
      Object result = query.getSingleResult();
      return getDoubleValue(result);
    } catch (Exception e) {
      log.error("Error getting average columns per dataset: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  @Transactional
  public void updateDatasetQualityScore(UUID datasetId, double score) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "UPDATE exploration_report SET quality_score = :score WHERE dataset_id = :datasetId");
      query.setParameter("score", score);
      query.setParameter("datasetId", datasetId);
      int updated = query.executeUpdate();
      log.info("Updated quality score for dataset: {} to {}", datasetId, score);
    } catch (Exception e) {
      log.error("Error updating dataset quality score: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to update dataset quality score", e);
    }
  }

  @Override
  @Transactional
  public void updateDatasetQualityScoreBulk(List<UUID> datasetIds, double score) {
    if (datasetIds == null || datasetIds.isEmpty()) {
      return;
    }
    try {
      Query query =
          entityManager.createNativeQuery(
              "UPDATE exploration_report SET quality_score = :score WHERE dataset_id IN"
                  + " (:datasetIds)");
      query.setParameter("score", score);
      query.setParameter("datasetIds", datasetIds);
      int updated = query.executeUpdate();
      log.info("Updated quality score for {} datasets to {}", updated, score);
    } catch (Exception e) {
      log.error("Error bulk updating dataset quality scores: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to bulk update dataset quality scores", e);
    }
  }

  @Override
  public double getQualityScoreByDatasetId(UUID datasetId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT r.qualityScore FROM ExplorationReport r WHERE r.dataset.id = :datasetId");
      query.setParameter("datasetId", datasetId);
      Object result = query.getSingleResult();
      return getDoubleValue(result);
    } catch (Exception e) {
      log.error("Error getting quality score for dataset: {}", e.getMessage(), e);
      return 0.0;
    }
  }

  @Override
  public List<Object[]> getQualityScoresByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT r.dataset.id, r.dataset.datasetName, r.qualityScore "
                  + "FROM ExplorationReport r "
                  + "WHERE r.dataset.sourceFile.project.id = :projectId "
                  + "ORDER BY r.qualityScore DESC");
      query.setParameter("projectId", projectId);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting quality scores by project: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  @Transactional
  public void markAsCleanedBulk(List<UUID> datasetIds) {
    if (datasetIds == null || datasetIds.isEmpty()) {
      return;
    }
    try {
      Query query =
          entityManager.createQuery(
              "UPDATE Dataset d SET d.isCleaned = true WHERE d.id IN :datasetIds");
      query.setParameter("datasetIds", datasetIds);
      int updated = query.executeUpdate();
      log.info("Marked {} datasets as cleaned", updated);
    } catch (Exception e) {
      log.error("Error bulk marking datasets as cleaned: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to bulk mark datasets as cleaned", e);
    }
  }

  @Override
  @Transactional
  public void markAsUncleanedBulk(List<UUID> datasetIds) {
    if (datasetIds == null || datasetIds.isEmpty()) {
      return;
    }
    try {
      Query query =
          entityManager.createQuery(
              "UPDATE Dataset d SET d.isCleaned = false WHERE d.id IN :datasetIds");
      query.setParameter("datasetIds", datasetIds);
      int updated = query.executeUpdate();
      log.info("Marked {} datasets as uncleaned", updated);
    } catch (Exception e) {
      log.error("Error bulk marking datasets as uncleaned: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to bulk mark datasets as uncleaned", e);
    }
  }

  @Override
  @Transactional
  public void deleteOldDatasets(LocalDateTime thresholdDate) {
    try {
      Query query =
          entityManager.createQuery(
              "DELETE FROM Dataset d WHERE d.createdAt < :thresholdDate AND d.isCleaned = true");
      query.setParameter("thresholdDate", thresholdDate);
      int deleted = query.executeUpdate();
      log.info("Deleted {} old datasets", deleted);
    } catch (Exception e) {
      log.error("Error deleting old datasets: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete old datasets", e);
    }
  }

  @Override
  @Transactional
  public void deleteDatasetsByProjectId(UUID projectId) {
    try {
      Query query =
          entityManager.createQuery(
              "DELETE FROM Dataset d WHERE d.sourceFile.project.id = :projectId");
      query.setParameter("projectId", projectId);
      int deleted = query.executeUpdate();
      log.info("Deleted {} datasets for project: {}", deleted, projectId);
    } catch (Exception e) {
      log.error("Error deleting datasets by project: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete datasets by project", e);
    }
  }

  @Override
  @Transactional
  public void deleteDatasetsByFileId(UUID fileId) {
    try {
      Query query =
          entityManager.createQuery("DELETE FROM Dataset d WHERE d.sourceFile.id = :fileId");
      query.setParameter("fileId", fileId);
      int deleted = query.executeUpdate();
      log.info("Deleted {} datasets for file: {}", deleted, fileId);
    } catch (Exception e) {
      log.error("Error deleting datasets by file: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to delete datasets by file", e);
    }
  }

  @Override
  public List<Object[]> getDatasetCountByMonth(int year) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT EXTRACT(MONTH FROM created_at) AS month, COUNT(dataset_id) "
                  + "FROM dataset "
                  + "WHERE EXTRACT(YEAR FROM created_at) = :year "
                  + "GROUP BY EXTRACT(MONTH FROM created_at) "
                  + "ORDER BY month");
      query.setParameter("year", year);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count by month: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> getDatasetCountByYear() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT EXTRACT(YEAR FROM created_at) AS year, COUNT(dataset_id) "
                  + "FROM dataset "
                  + "GROUP BY EXTRACT(YEAR FROM created_at) "
                  + "ORDER BY year DESC");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count by year: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> getDatasetCountByDay(LocalDateTime startDate, LocalDateTime endDate) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT DATE(created_at) AS day, COUNT(dataset_id) "
                  + "FROM dataset "
                  + "WHERE created_at BETWEEN :startDate AND :endDate "
                  + "GROUP BY DATE(created_at) "
                  + "ORDER BY day");
      query.setParameter("startDate", startDate);
      query.setParameter("endDate", endDate);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count by day: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> getDatasetCountGroupByProject() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT sf.project_id, p.project_name, COUNT(d.dataset_id) "
                  + "FROM dataset d "
                  + "JOIN source_file sf ON d.source_file_id = sf.file_id "
                  + "JOIN project p ON sf.project_id = p.project_id "
                  + "GROUP BY sf.project_id, p.project_name "
                  + "ORDER BY COUNT(d.dataset_id) DESC");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count group by project: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> getDatasetStatsByProject(UUID projectId) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT "
                  + "COUNT(d.dataset_id) AS total_datasets, "
                  + "SUM(CASE WHEN d.is_cleaned = true THEN 1 ELSE 0 END) AS cleaned_datasets, "
                  + "SUM(CASE WHEN d.is_cleaned = false THEN 1 ELSE 0 END) AS uncleaned_datasets, "
                  + "SUM(d.row_count) AS total_rows, "
                  + "AVG(d.row_count) AS avg_rows, "
                  + "SUM(d.column_count) AS total_columns, "
                  + "AVG(d.column_count) AS avg_columns "
                  + "FROM dataset d "
                  + "JOIN source_file sf ON d.source_file_id = sf.file_id "
                  + "WHERE sf.project_id = :projectId");
      query.setParameter("projectId", projectId);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset stats by project: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public Map<String, Object> getProjectDatasetStatistics(UUID projectId) {
    Map<String, Object> stats = new HashMap<>();
    try {
      List<Object[]> results = getDatasetStatsByProject(projectId);
      if (!results.isEmpty()) {
        Object[] row = results.get(0);
        stats.put("totalDatasets", getLongValue(row[0]));
        stats.put("cleanedDatasets", getLongValue(row[1]));
        stats.put("uncleanedDatasets", getLongValue(row[2]));
        stats.put("totalRows", getLongValue(row[3]));
        stats.put("averageRows", getDoubleValue(row[4]));
        stats.put("totalColumns", getLongValue(row[5]));
        stats.put("averageColumns", getDoubleValue(row[6]));
      }
    } catch (Exception e) {
      log.error("Error getting project dataset statistics: {}", e.getMessage(), e);
    }
    return stats;
  }

  @Override
  public List<Object[]> getDatasetCountGroupByFile() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT sf.file_id, sf.file_name, COUNT(d.dataset_id) "
                  + "FROM dataset d "
                  + "JOIN source_file sf ON d.source_file_id = sf.file_id "
                  + "GROUP BY sf.file_id, sf.file_name "
                  + "ORDER BY COUNT(d.dataset_id) DESC");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count group by file: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public Map<String, Object> getFileDatasetStatistics(UUID fileId) {
    Map<String, Object> stats = new HashMap<>();
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT "
                  + "COUNT(d.dataset_id) AS total_datasets, "
                  + "SUM(CASE WHEN d.is_cleaned = true THEN 1 ELSE 0 END) AS cleaned_datasets, "
                  + "SUM(d.row_count) AS total_rows, "
                  + "AVG(d.row_count) AS avg_rows, "
                  + "SUM(d.column_count) AS total_columns, "
                  + "AVG(d.column_count) AS avg_columns "
                  + "FROM dataset d "
                  + "WHERE d.source_file_id = :fileId");
      query.setParameter("fileId", fileId);
      Object[] result = (Object[]) query.getSingleResult();
      stats.put("totalDatasets", getLongValue(result[0]));
      stats.put("cleanedDatasets", getLongValue(result[1]));
      stats.put("totalRows", getLongValue(result[2]));
      stats.put("averageRows", getDoubleValue(result[3]));
      stats.put("totalColumns", getLongValue(result[4]));
      stats.put("averageColumns", getDoubleValue(result[5]));
    } catch (Exception e) {
      log.error("Error getting file dataset statistics: {}", e.getMessage(), e);
    }
    return stats;
  }

  @Override
  public List<Object[]> getDatasetCountGroupByCleanedStatus() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT is_cleaned, COUNT(dataset_id) FROM dataset GROUP BY is_cleaned");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting dataset count group by cleaned status: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public Map<String, Object> getCleanedStatusStatistics() {
    Map<String, Object> stats = new HashMap<>();
    try {
      List<Object[]> results = getDatasetCountGroupByCleanedStatus();
      for (Object[] row : results) {
        Boolean isCleaned = (Boolean) row[0];
        Long count = getLongValue(row[1]);
        if (isCleaned != null && isCleaned) {
          stats.put("cleaned", count);
        } else {
          stats.put("uncleaned", count);
        }
      }
      long total = getLongValue(stats.get("cleaned")) + getLongValue(stats.get("uncleaned"));
      stats.put("total", total);
    } catch (Exception e) {
      log.error("Error getting cleaned status statistics: {}", e.getMessage(), e);
    }
    return stats;
  }

  @Override
  public Map<String, Object> getQualityScoreStatistics() {
    Map<String, Object> stats = new HashMap<>();
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT "
                  + "AVG(quality_score) AS avg_score, "
                  + "MIN(quality_score) AS min_score, "
                  + "MAX(quality_score) AS max_score, "
                  + "COUNT(quality_score) AS total_scores "
                  + "FROM exploration_report");
      Object[] result = (Object[]) query.getSingleResult();
      stats.put("averageScore", getDoubleValue(result[0]));
      stats.put("minScore", getDoubleValue(result[1]));
      stats.put("maxScore", getDoubleValue(result[2]));
      stats.put("totalScores", getLongValue(result[3]));
    } catch (Exception e) {
      log.error("Error getting quality score statistics: {}", e.getMessage(), e);
    }
    return stats;
  }

  @Override
  public List<Object[]> getTopQualityDatasets(int limit) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT d.dataset_id, d.dataset_name, r.quality_score "
                  + "FROM dataset d "
                  + "JOIN exploration_report r ON d.dataset_id = r.dataset_id "
                  + "WHERE r.quality_score IS NOT NULL "
                  + "ORDER BY r.quality_score DESC "
                  + "LIMIT :limit");
      query.setParameter("limit", limit);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting top quality datasets: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> getBottomQualityDatasets(int limit) {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT d.dataset_id, d.dataset_name, r.quality_score "
                  + "FROM dataset d "
                  + "JOIN exploration_report r ON d.dataset_id = r.dataset_id "
                  + "WHERE r.quality_score IS NOT NULL "
                  + "ORDER BY r.quality_score ASC "
                  + "LIMIT :limit");
      query.setParameter("limit", limit);
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error getting bottom quality datasets: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  @Transactional
  public void updateDatasetRowCount(UUID datasetId, int rowCount) {
    try {
      Query query =
          entityManager.createQuery(
              "UPDATE Dataset d SET d.rowCount = :rowCount WHERE d.id = :datasetId");
      query.setParameter("rowCount", rowCount);
      query.setParameter("datasetId", datasetId);
      query.executeUpdate();
    } catch (Exception e) {
      log.error("Error updating dataset row count: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to update dataset row count", e);
    }
  }

  @Override
  @Transactional
  public void updateDatasetColumnCount(UUID datasetId, int columnCount) {
    try {
      Query query =
          entityManager.createQuery(
              "UPDATE Dataset d SET d.columnCount = :columnCount WHERE d.id = :datasetId");
      query.setParameter("columnCount", columnCount);
      query.setParameter("datasetId", datasetId);
      query.executeUpdate();
    } catch (Exception e) {
      log.error("Error updating dataset column count: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to update dataset column count", e);
    }
  }

  @Override
  @Transactional
  public void updateDatasetStats(UUID datasetId, int rowCount, int columnCount) {
    try {
      Query query =
          entityManager.createQuery(
              "UPDATE Dataset d SET d.rowCount = :rowCount, d.columnCount = :columnCount WHERE d.id"
                  + " = :datasetId");
      query.setParameter("rowCount", rowCount);
      query.setParameter("columnCount", columnCount);
      query.setParameter("datasetId", datasetId);
      query.executeUpdate();
    } catch (Exception e) {
      log.error("Error updating dataset stats: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to update dataset stats", e);
    }
  }

  @Override
  public List<Object[]> findDatasetsWithNoAnalysis() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT d.dataset_id, d.dataset_name, d.created_at "
                  + "FROM dataset d "
                  + "LEFT JOIN analysis_execution ae ON d.dataset_id = ae.dataset_id "
                  + "WHERE ae.execution_id IS NULL");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error finding datasets with no analysis: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public List<Object[]> findDatasetsWithNoCleaning() {
    try {
      Query query =
          entityManager.createNativeQuery(
              "SELECT d.dataset_id, d.dataset_name, d.created_at "
                  + "FROM dataset d "
                  + "LEFT JOIN cleaning_history ch ON d.dataset_id = ch.dataset_id "
                  + "WHERE ch.history_id IS NULL");
      return query.getResultList();
    } catch (Exception e) {
      log.error("Error finding datasets with no cleaning: {}", e.getMessage(), e);
      return List.of();
    }
  }

  @Override
  public long countDatasetsWithQualityScoreAbove(double threshold) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(r) FROM ExplorationReport r WHERE r.qualityScore > :threshold");
      query.setParameter("threshold", threshold);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error(
          "Error counting datasets with quality score above threshold: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countDatasetsWithQualityScoreBelow(double threshold) {
    try {
      Query query =
          entityManager.createQuery(
              "SELECT COUNT(r) FROM ExplorationReport r WHERE r.qualityScore < :threshold");
      query.setParameter("threshold", threshold);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error(
          "Error counting datasets with quality score below threshold: {}", e.getMessage(), e);
      return 0L;
    }
  }

  private Long getLongValue(Object value) {
    if (value == null) return 0L;
    if (value instanceof Long) return (Long) value;
    if (value instanceof Integer) return ((Integer) value).longValue();
    if (value instanceof Number) return ((Number) value).longValue();
    return 0L;
  }

  private Double getDoubleValue(Object value) {
    if (value == null) return 0.0;
    if (value instanceof Double) return (Double) value;
    if (value instanceof Integer) return ((Integer) value).doubleValue();
    if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
    if (value instanceof Number) return ((Number) value).doubleValue();
    return 0.0;
  }
}
