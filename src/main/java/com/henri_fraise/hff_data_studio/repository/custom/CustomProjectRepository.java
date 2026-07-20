package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CustomProjectRepository {

	ProjectStatisticsResponse getProjectStatistics();

	long countProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

	long countActiveProjectsByUser(UUID userId);

	void archiveInactiveProjects(LocalDateTime olderThan);

	void archiveProjectByUserId(UUID userId);
}