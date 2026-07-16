package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
	private Long totalUsers;
	private Long activeUsers;
	private Long inactiveUsers;
	private Long totalCategories;
	private Long newUsersLast30Days;
	private Long newUsersLast7Days;
	private Long activeUsersLast30Days;
	private Long activeUsersLast7Days;
}