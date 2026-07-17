package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CustomUserRepository {

	// ==================== STATISTICS ====================

	UserStatisticsResponse getUserStatistics();

	UserStatisticsResponse getUserStatisticsByCategoryId(UUID categoryId);

	Map<String, Object> getUserStatisticsMap();

	// ==================== COUNT METHODS ====================

	long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

	long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

	long countUsersByCategoryId(UUID categoryId);

	long countUsersByCategoryLabel(String categoryLabel);

	long countUsersCreatedAfter(LocalDateTime date);

	long countUsersCreatedBefore(LocalDateTime date);

	long countUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

	long countUsersWithLastLoginAfter(LocalDateTime date);

	long countUsersWithLastLoginBefore(LocalDateTime date);

	// ==================== GROUP BY METHODS ====================

	List<Object[]> countGroupByCategory();

	List<Object[]> countGroupByCategoryWithDetails();

	List<Object[]> countByMonth(int year);

	List<Object[]> countByYear();

	List<Object[]> countGroupByMonth(int year);

	List<Object[]> countGroupByYear();

	List<Object[]> countGroupByDay(LocalDateTime startDate, LocalDateTime endDate);

	List<Object[]> countGroupByStatus();

	// ==================== ACTIVITY METHODS ====================

	void updateUserActivity(UUID userId, String activity);

	void updateUserLastLogin(UUID userId, LocalDateTime lastLogin);

	void updateUserActivityBulk(List<UUID> userIds, String activity);

	// ==================== STATISTICS BY PERIOD ====================

	Map<String, Object> getStatisticsByPeriod(LocalDateTime startDate, LocalDateTime endDate);

	Map<String, Object> getMonthlyStatistics(int year, int month);

	Map<String, Object> getYearlyStatistics(int year);

	// ==================== USER ACTIVITY STATISTICS ====================

	Map<String, Object> getUserActivityStatistics();

	List<Object[]> getMostActiveUsers(int limit);

	List<Object[]> getLeastActiveUsers(int limit);

	double getAverageLoginFrequency();

	// ==================== CATEGORY STATISTICS ====================

	Map<String, Object> getCategoryStatistics();

	List<Object[]> getUsersByCategoryWithCounts();

	// ==================== BULK OPERATIONS ====================

	void deactivateInactiveUsers(LocalDateTime thresholdDate);

	void activateUsersBulk(List<UUID> userIds);

	void deactivateUsersBulk(List<UUID> userIds);

	void deleteUsersBulk(List<UUID> userIds);

	// ==================== CUSTOM QUERIES ====================

	List<Object[]> findUsersWithNoActivity();

	List<Object[]> findUsersWithRecentActivity(int days);

	long countUsersWithNoLogin();

	long countUsersWithLoginInLastDays(int days);
}