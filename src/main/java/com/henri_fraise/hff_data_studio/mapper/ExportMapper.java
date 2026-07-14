package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ExportResponse;
import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import org.springframework.stereotype.Component;

@Component
public class ExportMapper {

	public ExportResponse toResponse(Export export) {
		String EXPORT_PATH = "/api/v1/exports/";
		if (export == null) return null;

		return ExportResponse.builder()
				.exportId(export.getId())
				.exportFormat(export.getExportFomat())
				.formatLabel(getFormatLabel(export.getExportFomat()))
				.exportedAt(export.getExportAt())
				.filePath(export.getFilePath())
				.fileName(extractFileName(export.getFilePath()))
				.fileSize(export.getFileSize() != null ? export.getFileSize() : null)
				.fileSizeFormatted(formatFileSize(export.getFileSize()))
				.executionId(export.getExecution() != null ? export.getExecution().getId() : null)
				.executionStatus(
						export.getExecution() != null && export.getExecution().getStatus() != null
								? AnalysisExecutionStatus.valueOf(export.getExecution().getStatus().name())
								: null
				)
				.userId(export.getUser() != null ? export.getUser().getId() : null)
				.userFullName(
						export.getUser() != null
								? export.getUser().getFirstName() + " " + export.getUser().getLastName()
								: null
				)
				.downloadUrl(EXPORT_PATH + export.getId() + "/download")
				.build();
	}

	private String extractFileName(String filePath) {
		if (filePath == null) return null;
		int lastSlash = filePath.lastIndexOf("/");
		if (lastSlash == -1)
			lastSlash = filePath.lastIndexOf("\\");
		return lastSlash == -1 ? filePath : filePath.substring(lastSlash + 1);
	}
}
