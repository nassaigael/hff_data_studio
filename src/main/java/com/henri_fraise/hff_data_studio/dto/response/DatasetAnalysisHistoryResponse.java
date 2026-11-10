package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DatasetAnalysisHistoryResponse(
		UUID executionId,
		String analysisName,
		String analysisCategory,
		String status,
		LocalDateTime executedAt,
		Integer durationMs,
		String userName,
		String userEmail) {}