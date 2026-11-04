package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record DatasetUsageSummaryResponse(
		long analysesCount,
		long exportsCount,
		long uniqueUsersCount,
		long commentsCount,
		LocalDateTime lastActivityAt,
		List<String> tags) {}