package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import org.springframework.stereotype.Component;

@Component
public class DatasetMapper {
	public DatasetResponse toResponse(Dataset dataset) {
		if (dataset == null)
			return null;

		return DatasetResponse.builder()
				.datasetId(dataset.getId())
				.datasetName(dataset.getDatasetName())
				.rowCount(dataset.getRowCount())
				.columnCount(dataset.getColumnCount())
				.createdAt(dataset.getCreatedAt())
				.isCleaned(dataset.getIsCleaned())
				.fileId(dataset.getSourceFile() != null ? dataset.getSourceFile().getId() : null)
				.fileName(dataset.getSourceFile() != null ? dataset.getSourceFile().getFileName() : null)
				.projectId(
						dataset.getSourceFile() != null && dataset.getSourceFile().getProject() != null
								? dataset.getSourceFile().getProject().getId()
								: null
				)
				.projectName(
						dataset.getSourceFile() != null && dataset.getSourceFile().getProject() != null
								? dataset.getSourceFile().getProject().getProjectName()
								: null
				)
				.qualityScore(
						dataset.getExplorationReport() != null
								? dataset.getExplorationReport().getQualityScore().intValue()
								: null
				)
				.analysisCount(
						(int) (dataset.getAnalysisExecutions() != null
								? (long) dataset.getAnalysisExecutions().size()
								: 0L)
				)
				.build();
	}
}
