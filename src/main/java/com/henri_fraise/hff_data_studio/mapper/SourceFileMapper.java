package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import org.springframework.stereotype.Component;

@Component
public class SourceFileMapper {
	public SourceFileResponse toResponse(SourceFile file) {
		if (file == null)
			return null;

		return SourceFileResponse.builder()
				.fileId(file.getId())
				.fileName(file.getFileName())
				.fileType(file.getFileType())
				.storagePath(file.getStoragePath())
				.sizesBytes(file.getSizeBytes())
				.sizesFormatted(formatFileSize(file.getSizeBytes()))
				.uploadAt(file.getUploadAt())
				.processingStatus(file.getProcessingStatus())
				.projectId(file.getProject().getId() != null ? file.getProject().getId() : null)
				.projectName(file.getProject().getProjectName() != null ? file.getProject().getProjectName() : null)
				.userId(file.getUser().getId() != null ? file.getUser().getId() : null)
				.userFullName(
						file.getUser() != null
								? file.getUser().getFirstName() + " " + file.getUser().getLastName()
								: null
				)
				.datasetCount(file.getDatasets() != null ? file.getDatasets().size() : 0)
				.build();
	}

	private String formatFileSize(Long bytes) {
		if (bytes == null)
			return "0 B";
		if (bytes < 1024)
			return bytes + " B";
		if (bytes < 1024 * 1024)
			return String.format("%.2f KB", bytes / 1024.0);
		if (bytes < 1024 * 1024 * 1024)
			return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
		return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
	}
}
