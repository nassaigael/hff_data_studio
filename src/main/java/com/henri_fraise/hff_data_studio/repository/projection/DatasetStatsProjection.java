package com.henri_fraise.hff_data_studio.repository.projection;

public interface DatasetStatsProjection {

	Long getTotalDatasets();

	Long getTotalRows();

	Long getTotalColumns();

	Long getCleanedDatasets();

	Long getUncleanedDatasets();

	Double getAvgQualityScore();

	Long getNewDatasetsLast30Days();

	Long getTotalAnalysesRun();

	Long getTotalExports();
}