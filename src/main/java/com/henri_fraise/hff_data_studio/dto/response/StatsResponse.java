package com.henri_fraise.hff_data_studio.dto.response;

public record StatsResponse(
		Long totalDatasets,
		Long totalRows,
		Long totalColumns,
		Long cleanedDatasets,
		Long uncleanedDatasets,
		Double avgQualityScore,
		Long newDatasetsLast30Days,
		Long totalAnalysesRun,
		Long totalExports
) {}