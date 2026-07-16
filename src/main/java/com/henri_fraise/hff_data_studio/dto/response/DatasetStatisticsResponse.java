package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetStatisticsResponse {
	private Long totalDatasets;
	private Long totalRows;
	private Long totalColumns;
	private Long cleanedDatasets;
	private Long uncleanedDatasets;
	private Double averageQualityScore;
	private Long newDatasetsLast30Days;
}