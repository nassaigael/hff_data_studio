package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import org.springframework.stereotype.Component;

@Component
public class ExplorationReportMapper {

	public ExplorationReportResponse toResponse(ExplorationReport report) {
		if (report == null)
			return null;

		return ExplorationReportResponse.builder()
				.reportId(report.getId())
				.generatedAt(report.getGeneratedAt())
				.totalRows(report.getTotalRows())
				.duplicateCount(report.getDuplicateCount())
				.missingValueCount(report.getMissingValuesCount())
				.qualityScore(report.getQualityScore())
				.reportPdfPath(report.getReportPdfPath())
				.datasetId(report.getDataset() != null ? report.getDataset().getId() : null)
				.datasetName(report.getDataset() != null ? report.getDataset().getDatasetName() : null)
				.build();
	}
}
