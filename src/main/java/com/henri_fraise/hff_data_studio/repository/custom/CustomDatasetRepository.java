package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomDatasetRepository {

	DatasetStatisticsResponse getDatasetStatistics();

	long countDatasetsByProjectId(UUID projectId);

	long countCleanedDatasetsByProjectId(UUID projectId);

	double getAverageDatasetQualityScore();

	void updateDatasetQualityScore(UUID datasetId, double score);
}