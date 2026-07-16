package com.henri_fraise.hff_data_studio.mapper;

import static com.henri_fraise.hff_data_studio.mapper.ExportMapper.getBytesString;

import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import org.springframework.stereotype.Component;

@Component
public class SourceFileMapper {
  public SourceFileResponse toResponse(SourceFile file) {
    if (file == null) return null;

    return SourceFileResponse.builder()
        .fileId(file.getId())
        .fileName(file.getFileName())
        .fileFormat(file.getFileFormat())
        .storagePath(file.getStoragePath())
        .sizesBytes(file.getSizeBytes())
        .sizesFormatted(formatFileSize(file.getSizeBytes()))
        .uploadAt(file.getUploadedAt())
        .processingStatus(file.getProcessingStatus())
        .projectId(file.getProject().getId() != null ? file.getProject().getId() : null)
        .projectName(
            file.getProject().getProjectName() != null ? file.getProject().getProjectName() : null)
        .userId(file.getUser().getId() != null ? file.getUser().getId() : null)
        .userFullName(
            file.getUser() != null
                ? file.getUser().getFirstName() + " " + file.getUser().getLastName()
                : null)
        .datasetCount(file.getDatasets() != null ? file.getDatasets().size() : 0)
        .build();
  }

  private String formatFileSize(Long bytes) {
    if (bytes == null) return "0 B";
    return getBytesString(bytes);
  }
}
