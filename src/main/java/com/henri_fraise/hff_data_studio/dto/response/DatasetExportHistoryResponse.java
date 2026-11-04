package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DatasetExportHistoryResponse(
		UUID exportId,
		String format,
		LocalDateTime createdAt,
		Long sizeBytes,
		String userName,
		String userEmail,
		String downloadUrl) {}