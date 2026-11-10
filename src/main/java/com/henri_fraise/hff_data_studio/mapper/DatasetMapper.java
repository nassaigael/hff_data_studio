package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import org.springframework.stereotype.Component;

@Component
public class DatasetMapper {

  public DatasetResponse toResponse(Dataset dataset) {
    if (dataset == null) return null;

    SourceFile sourceFile = dataset.getSourceFile();

    return DatasetResponse.builder()
            .datasetId(dataset.getId())
            .datasetName(dataset.getDatasetName())
            .rowCount(dataset.getRowCount())
            .columnCount(dataset.getColumnCount())
            .createdAt(dataset.getCreatedAt())
            .isCleaned(dataset.getIsCleaned())
            .fileId(sourceFile != null ? sourceFile.getId() : null)
            .fileName(sourceFile != null ? sourceFile.getFileName() : null)
            .fileFormat(sourceFile != null ? sourceFile.getFileType() : null)
            .processingStatus(sourceFile != null ? sourceFile.getProcessingStatus() : null)
            .projectId(
                    sourceFile != null && sourceFile.getProject() != null
                            ? sourceFile.getProject().getId()
                            : null)
            .projectName(
                    sourceFile != null && sourceFile.getProject() != null
                            ? sourceFile.getProject().getProjectName()
                            : null)
            .qualityScore(
                    dataset.getExplorationReport() != null
                            && dataset.getExplorationReport().getQualityScore() != null
                            ? dataset.getExplorationReport().getQualityScore().intValue()
                            : null)
            .analysisCount(
                    dataset.getAnalysisExecutions() != null
                            ? dataset.getAnalysisExecutions().size()
                            : 0)
            .build();
  }
}