package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CustomDatasetRepository {

  DatasetStatisticsResponse getDatasetStatistics();

  Map<String, Object> getDatasetStatisticsMap();

  double getAverageDatasetQualityScore();

  double getAverageDatasetQualityScoreByProjectId(UUID projectId);

  long getTotalRows();

  long getTotalRowsByProjectId(UUID projectId);

  long getTotalColumns();

  long getTotalColumnsByProjectId(UUID projectId);

  double getAverageRowsPerDataset();

  double getAverageColumnsPerDataset();

  long countDatasetsByProjectId(UUID projectId);

  long countCleanedDatasetsByProjectId(UUID projectId);

  long countUncleanedDatasetsByProjectId(UUID projectId);

  long countDatasetsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

  long countDatasetsCreatedAfter(LocalDateTime date);

  long countDatasetsCreatedBefore(LocalDateTime date);

  void updateDatasetQualityScore(UUID datasetId, double score);

  void updateDatasetQualityScoreBulk(List<UUID> datasetIds, double score);

  double getQualityScoreByDatasetId(UUID datasetId);

  List<Object[]> getQualityScoresByProjectId(UUID projectId);

  void markAsCleanedBulk(List<UUID> datasetIds);

  void markAsUncleanedBulk(List<UUID> datasetIds);

  void deleteOldDatasets(LocalDateTime thresholdDate);

  void deleteDatasetsByProjectId(UUID projectId);

  void deleteDatasetsByFileId(UUID fileId);

  List<Object[]> getDatasetCountByMonth(int year);

  List<Object[]> getDatasetCountByYear();

  List<Object[]> getDatasetCountByDay(LocalDateTime startDate, LocalDateTime endDate);

  List<Object[]> getDatasetCountGroupByProject();

  List<Object[]> getDatasetStatsByProject(UUID projectId);

  Map<String, Object> getProjectDatasetStatistics(UUID projectId);

  List<Object[]> getDatasetCountGroupByFile();

  Map<String, Object> getFileDatasetStatistics(UUID fileId);

  List<Object[]> getDatasetCountGroupByCleanedStatus();

  Map<String, Object> getCleanedStatusStatistics();

  Map<String, Object> getQualityScoreStatistics();

  List<Object[]> getTopQualityDatasets(int limit);

  List<Object[]> getBottomQualityDatasets(int limit);

  void updateDatasetRowCount(UUID datasetId, int rowCount);

  void updateDatasetColumnCount(UUID datasetId, int columnCount);

  void updateDatasetStats(UUID datasetId, int rowCount, int columnCount);

  List<Object[]> findDatasetsWithNoAnalysis();

  List<Object[]> findDatasetsWithNoCleaning();

  long countDatasetsWithQualityScoreAbove(double threshold);

  long countDatasetsWithQualityScoreBelow(double threshold);
}
