package com.henri_fraise.hff_data_studio.dto;

public record StatsDto(
		Long totalDatasets,
		Long totalRows,
		Long totalColumns,
		Long cleanedDatasets,
		Long uncleanedDatasets,
		Double avgQualityScore,
		Long newDatasetsLast30Days
) {
}