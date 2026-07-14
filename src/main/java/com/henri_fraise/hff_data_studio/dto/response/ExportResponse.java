package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportResponse {
	private UUID exportId;
	private ExportFormat exportFormat;
	private String formatLabel;
	private LocalDateTime exportedAt;
	private String filePath;
	private String fileName;
	private String fileSizeFormatted;
	private UUID executionId;
	private AnalysisExecutionStatus executionStatus;
	private UUID userId;
	private String userFullName;
	private String downloadUrl;
}
