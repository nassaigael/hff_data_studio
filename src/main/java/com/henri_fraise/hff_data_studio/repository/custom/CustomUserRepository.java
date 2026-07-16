package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;

import java.time.LocalDateTime;

public interface CustomUserRepository {
	UserStatisticsResponse getUserStatistics();
	long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
	long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
	void updateUserActivity(String userId, String activity);
}
