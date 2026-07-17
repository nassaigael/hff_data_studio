package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

	private Long newUsersLast24Hours;

	private Long newUsersToday;

	private Long activeUsersLast30Days;

	private Long activeUsersLast7Days;

	private Long activeUsersLast24Hours;

	private Long activeUsersToday;

	private Long usersWithLogin;

	private Long usersWithoutLogin;

	private Double averageLoginFrequency;

	private Double averageDaysSinceLastLogin;

	private LocalDateTime firstUser;

	private LocalDateTime lastUser;

	private LocalDateTime lastLoginEver;

	private LocalDateTime firstLoginEver;

	private Double activityRate;

	private Double activityRateLast30Days;

	private Double activityRateLast7Days;

	private Long adminCount;

	private Long analystCount;

	private Long consultantCount;

	private Long guestCount;

	private Long usersWithCustomCategory;

	private Double activePercentage;

	private Double inactivePercentage;

	private Double newUsersPercentage;

	private Double adminPercentage;

	private Double analystPercentage;

	private Double consultantPercentage;

	private Double guestPercentage;

	private Long growthLast30Days;

	private Long growthLast7Days;

	private Double growthPercentageLast30Days;

	private Double growthPercentageLast7Days;

	private LocalDateTime statisticsGeneratedAt;

	public Double getActivePercentage() {
		if (totalUsers != null && totalUsers > 0 && activeUsers != null) {
			return (activeUsers.doubleValue() / totalUsers.doubleValue()) * 100;
		}
		return 0.0;
	}

	public Double getInactivePercentage() {
		if (totalUsers != null && totalUsers > 0 && inactiveUsers != null) {
			return (inactiveUsers.doubleValue() / totalUsers.doubleValue()) * 100;
		}
		return 0.0;
	}

	public Double getNewUsersPercentage() {
		if (totalUsers != null && totalUsers > 0 && newUsersLast30Days != null) {
			return (newUsersLast30Days.doubleValue() / totalUsers.doubleValue()) * 100;
		}
		return 0.0;
	}

	public String getSummary() {
		return String.format(
				"Total: %d | Active: %d (%.1f%%) | Inactive: %d | New (30d): %d",
				totalUsers != null ? totalUsers : 0,
				activeUsers != null ? activeUsers : 0,
				getActivePercentage(),
				inactiveUsers != null ? inactiveUsers : 0,
				newUsersLast30Days != null ? newUsersLast30Days : 0
		);
	}
}