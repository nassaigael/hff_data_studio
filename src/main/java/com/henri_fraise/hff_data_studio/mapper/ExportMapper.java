package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ExportResponse;
import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ExportMapper {

  public ExportResponse toResponse(Export export) {
    String EXPORT_PATH = "/api/v1/exports/";
    if (export == null) return null;

    return ExportResponse.builder()
        .exportId(export.getId())
        .exportFormat(export.getExportFormat())
        .formatLabel(getFormatLabel(export.getExportFormat()))
        .exportedAt(export.getExportedAt())
        .filePath(export.getFilePath())
        .fileName(extractFileName(export.getFilePath()))
        .fileSize(export.getFileSize() != null ? export.getFileSize() : null)
        .fileSizeFormatted(formatFileSize(export.getFileSize()))
        .executionId(export.getExecution() != null ? export.getExecution().getId() : null)
        .executionStatus(
            export.getExecution() != null && export.getExecution().getStatus() != null
                ? AnalysisExecutionStatus.valueOf(export.getExecution().getStatus().name())
                : null)
        .userId(export.getUser() != null ? export.getUser().getId() : null)
        .userFullName(
            export.getUser() != null
                ? export.getUser().getFirstName() + " " + export.getUser().getLastName()
                : null)
        .downloadUrl(EXPORT_PATH + export.getId() + "/download")
        .build();
  }

  private String formatFileSize(Long bytes) {
    if (bytes == null || bytes == 0) return "0 B";
    return getBytesString(bytes);
  }

  @NonNull
  static String getBytesString(Long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
  }

  private String getFormatLabel(ExportFormat format) {
    if (format == null) return null;

    return switch (format) {
      case CSV -> "CSV File";
      case XLSX -> "Excel File";
      case PNG -> "PNG Image";
      case ZIP -> "ZIP Archive";
      case PDF -> "PDF Document";
      case HTML -> "HTML File";
      case JSON -> "JSON File";
      default -> format.name();
    };
  }

  private String extractFileName(String filePath) {
    if (filePath == null) return null;
    int lastSlash = filePath.lastIndexOf("/");
    if (lastSlash == -1) lastSlash = filePath.lastIndexOf("\\");
    return lastSlash == -1 ? filePath : filePath.substring(lastSlash + 1);
  }
}
