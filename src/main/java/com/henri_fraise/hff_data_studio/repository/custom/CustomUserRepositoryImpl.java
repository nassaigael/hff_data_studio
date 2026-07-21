package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomUserRepositoryImpl implements CustomUserRepository {

	@PersistenceContext
	private final EntityManager entityManager;

	@Override
	public UserStatisticsResponse getUserStatistics() {
		try {
			String sql = """
                    SELECT 
                        COUNT(u.user_id) AS totalUsers,
                        SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END) AS activeUsers,
                        SUM(CASE WHEN u.is_active = false THEN 1 ELSE 0 END) AS inactiveUsers,
                        COUNT(DISTINCT u.category_id) AS totalCategories,
                        MIN(u.created_at) AS firstUser,
                        MAX(u.created_at) AS lastUser,
                        AVG(CASE WHEN u.last_login IS NOT NULL THEN 1 ELSE 0 END) * 100 AS activityRate
                    FROM users u
                    """;

			Query query = entityManager.createNativeQuery(sql);
			Object[] result = (Object[]) query.getSingleResult();

			return UserStatisticsResponse.builder()
					.totalUsers(getLongValue(result[0]))
					.activeUsers(getLongValue(result[1]))
					.inactiveUsers(getLongValue(result[2]))
					.totalCategories(getLongValue(result[3]))
					.firstUser((LocalDateTime) result[4])
					.lastUser((LocalDateTime) result[5])
					.activityRate(getDoubleValue(result[6]))
					.build();

		} catch (Exception e) {
			log.error("Error getting user statistics: {}", e.getMessage(), e);
			return UserStatisticsResponse.builder()
					.totalUsers(0L)
					.activeUsers(0L)
					.inactiveUsers(0L)
					.totalCategories(0L)
					.activityRate(0.0)
					.build();
		}
	}

	@Override
	public UserStatisticsResponse getUserStatisticsByCategoryId(UUID categoryId) {
		try {
			String sql = """
                    SELECT 
                        COUNT(u.user_id) AS totalUsers,
                        SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END) AS activeUsers,
                        SUM(CASE WHEN u.is_active = false THEN 1 ELSE 0 END) AS inactiveUsers,
                        MIN(u.created_at) AS firstUser,
                        MAX(u.created_at) AS lastUser
                    FROM users u
                    WHERE u.category_id = :categoryId
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("categoryId", categoryId);
			Object[] result = (Object[]) query.getSingleResult();

			return UserStatisticsResponse.builder()
					.totalUsers(getLongValue(result[0]))
					.activeUsers(getLongValue(result[1]))
					.inactiveUsers(getLongValue(result[2]))
					.firstUser((LocalDateTime) result[3])
					.lastUser((LocalDateTime) result[4])
					.build();

		} catch (Exception e) {
			log.error("Error getting user statistics by category: {}", e.getMessage(), e);
			return UserStatisticsResponse.builder()
					.totalUsers(0L)
					.activeUsers(0L)
					.inactiveUsers(0L)
					.build();
		}
	}

	@Override
	public Map<String, Object> getUserStatisticsMap() {
		Map<String, Object> stats = new HashMap<>();
		try {
			UserStatisticsResponse response = getUserStatistics();
			stats.put("totalUsers", response.getTotalUsers());
			stats.put("activeUsers", response.getActiveUsers());
			stats.put("inactiveUsers", response.getInactiveUsers());
			stats.put("totalCategories", response.getTotalCategories());
			stats.put("firstUser", response.getFirstUser());
			stats.put("lastUser", response.getLastUser());
			stats.put("activityRate", response.getActivityRate());

			// Additional stats
			String sql = """
                    SELECT 
                        AVG(CASE WHEN u.is_active = true AND u.last_login IS NOT NULL 
                            THEN EXTRACT(DAY FROM (NOW() - u.last_login)) ELSE NULL END) AS avgDaysSinceLastLogin
                    FROM users u
                    """;

			Query query = entityManager.createNativeQuery(sql);
			Object result = query.getSingleResult();
			stats.put("averageDaysSinceLastLogin", getDoubleValue(result));

		} catch (Exception e) {
			log.error("Error getting user statistics map: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		try {
			String sql = """
                    SELECT COUNT(u.user_id) 
                    FROM users u
                    WHERE u.is_active = true 
                    AND u.last_login BETWEEN :startDate AND :endDate
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting active users by date range: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		try {
			String sql = """
                    SELECT COUNT(u.user_id) 
                    FROM users u
                    WHERE u.created_at BETWEEN :startDate AND :endDate
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting new users by date range: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersByCategoryId(UUID categoryId) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.category_id = :categoryId";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("categoryId", categoryId);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users by category: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersByCategoryLabel(String categoryLabel) {
		try {
			String sql = """
                    SELECT COUNT(u.user_id) 
                    FROM users u
                    JOIN user_category uc ON u.category_id = uc.category_id
                    WHERE uc.label = :categoryLabel
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("categoryLabel", categoryLabel);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users by category label: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersCreatedAfter(LocalDateTime date) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.created_at > :date";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("date", date);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users created after: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersCreatedBefore(LocalDateTime date) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.created_at < :date";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("date", date);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users created before: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.created_at BETWEEN :startDate AND :endDate";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users created between: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersWithLastLoginAfter(LocalDateTime date) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.last_login > :date";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("date", date);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users with last login after: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersWithLastLoginBefore(LocalDateTime date) {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.last_login < :date";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("date", date);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users with last login before: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	@Transactional
	public void updateUserActivity(UUID userId, String activity) {
		try {
			String sql = "UPDATE users SET last_activity = NOW(), activity_log = CONCAT(activity_log, :activity) WHERE user_id = :userId";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userId", userId);
			query.setParameter("activity", activity + "|");
			int updated = query.executeUpdate();
			if (updated > 0) {
				log.debug("Updated activity for user: {}", userId);
			}
		} catch (Exception e) {
			log.error("Error updating user activity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to update user activity", e);
		}
	}

	@Override
	@Transactional
	public void updateUserLastLogin(UUID userId, LocalDateTime lastLogin) {
		try {
			String sql = "UPDATE users SET last_login = :lastLogin WHERE user_id = :userId";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userId", userId);
			query.setParameter("lastLogin", lastLogin);
			query.executeUpdate();
		} catch (Exception e) {
			log.error("Error updating user last login: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to update user last login", e);
		}
	}

	@Override
	@Transactional
	public void updateUserActivityBulk(List<UUID> userIds, String activity) {
		try {
			if (userIds == null || userIds.isEmpty()) {
				return;
			}
			String sql = "UPDATE users SET last_activity = NOW(), activity_log = CONCAT(activity_log, :activity) WHERE user_id IN (:userIds)";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userIds", userIds);
			query.setParameter("activity", activity + "|");
			int updated = query.executeUpdate();
			log.info("Updated activity for {} users", updated);
		} catch (Exception e) {
			log.error("Error bulk updating user activity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to bulk update user activity", e);
		}
	}

	@Override
	public List<Object[]> countGroupByCategory() {
		try {
			String sql = """
                    SELECT uc.label, COUNT(u.user_id) 
                    FROM users u
                    JOIN user_category uc ON u.category_id = uc.category_id
                    GROUP BY uc.label
                    ORDER BY COUNT(u.user_id) DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by category: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countGroupByCategoryWithDetails() {
		try {
			String sql = """
                    SELECT 
                        uc.label,
                        COUNT(u.user_id) AS total,
                        SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END) AS active,
                        SUM(CASE WHEN u.is_active = false THEN 1 ELSE 0 END) AS inactive
                    FROM users u
                    JOIN user_category uc ON u.category_id = uc.category_id
                    GROUP BY uc.label
                    ORDER BY total DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by category with details: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countByMonth(int year) {
		try {
			String sql = """
                    SELECT\s
                        EXTRACT(MONTH FROM created_at) AS month,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active,
                        SUM(CASE WHEN is_active = false THEN 1 ELSE 0 END) AS inactive,
                        MIN(created_at) AS firstUser,
                        MAX(created_at) AS lastUser
                    FROM users
                    WHERE EXTRACT(YEAR FROM created_at) = :year
                    GROUP BY EXTRACT(MONTH FROM created_at)
                    ORDER BY month
                   \s""";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users by month for year {}: {}", year, e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countByMonth() {
		int year = LocalDateTime.now().getYear();
		try {
			String sql = """
                    SELECT\s
                        EXTRACT(MONTH FROM created_at) AS month,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active,
                        SUM(CASE WHEN is_active = false THEN 1 ELSE 0 END) AS inactive,
                        MIN(created_at) AS firstUser,
                        MAX(created_at) AS lastUser
                    FROM users
                    WHERE EXTRACT(YEAR FROM created_at) = :year
                    GROUP BY EXTRACT(MONTH FROM created_at)
                    ORDER BY month
                   \s""";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users by month for year {}: {}", year, e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countByYear() {
		try {
			String sql = """
                    SELECT 
                        EXTRACT(YEAR FROM created_at) AS year,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active,
                        SUM(CASE WHEN is_active = false THEN 1 ELSE 0 END) AS inactive,
                        MIN(created_at) AS firstUser,
                        MAX(created_at) AS lastUser
                    FROM users
                    GROUP BY EXTRACT(YEAR FROM created_at)
                    ORDER BY year DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users by year: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countGroupByMonth(int year) {
		try {
			String sql = """
                    SELECT 
                        EXTRACT(MONTH FROM created_at) AS month,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active
                    FROM users
                    WHERE EXTRACT(YEAR FROM created_at) = :year
                    GROUP BY EXTRACT(MONTH FROM created_at)
                    ORDER BY month
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by month: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countGroupByYear() {
		try {
			String sql = """
                    SELECT 
                        EXTRACT(YEAR FROM created_at) AS year,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active
                    FROM users
                    GROUP BY EXTRACT(YEAR FROM created_at)
                    ORDER BY year DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by year: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countGroupByDay(LocalDateTime startDate, LocalDateTime endDate) {
		try {
			String sql = """
                    SELECT 
                        DATE(created_at) AS day,
                        COUNT(user_id) AS total,
                        SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) AS active
                    FROM users
                    WHERE created_at BETWEEN :startDate AND :endDate
                    GROUP BY DATE(created_at)
                    ORDER BY day
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by day: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> countGroupByStatus() {
		try {
			String sql = """
                    SELECT 
                        is_active,
                        COUNT(user_id) AS total,
                        MIN(created_at) AS firstCreated,
                        MAX(created_at) AS lastCreated
                    FROM users
                    GROUP BY is_active
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error counting users grouped by status: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public Map<String, Object> getStatisticsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
		Map<String, Object> stats = new HashMap<>();
		try {
			String sql = """
                    SELECT 
                        COUNT(u.user_id) AS totalUsers,
                        SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END) AS activeUsers,
                        SUM(CASE WHEN u.is_active = false THEN 1 ELSE 0 END) AS inactiveUsers,
                        COUNT(CASE WHEN u.created_at BETWEEN :startDate AND :endDate THEN 1 END) AS newUsers,
                        COUNT(CASE WHEN u.last_login BETWEEN :startDate AND :endDate THEN 1 END) AS activeLogins
                    FROM users u
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("startDate", startDate);
			query.setParameter("endDate", endDate);
			Object[] result = (Object[]) query.getSingleResult();

			stats.put("totalUsers", getLongValue(result[0]));
			stats.put("activeUsers", getLongValue(result[1]));
			stats.put("inactiveUsers", getLongValue(result[2]));
			stats.put("newUsers", getLongValue(result[3]));
			stats.put("activeLogins", getLongValue(result[4]));
			stats.put("periodStart", startDate);
			stats.put("periodEnd", endDate);

		} catch (Exception e) {
			log.error("Error getting statistics by period: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public Map<String, Object> getMonthlyStatistics(int year, int month) {
		Map<String, Object> stats = new HashMap<>();
		try {
			LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
			LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

			stats.putAll(getStatisticsByPeriod(startDate, endDate));
			stats.put("year", year);
			stats.put("month", month);

		} catch (Exception e) {
			log.error("Error getting monthly statistics: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public Map<String, Object> getYearlyStatistics(int year) {
		Map<String, Object> stats = new HashMap<>();
		try {
			LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
			LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59);

			stats.putAll(getStatisticsByPeriod(startDate, endDate));
			stats.put("year", year);

		} catch (Exception e) {
			log.error("Error getting yearly statistics: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public Map<String, Object> getUserActivityStatistics() {
		Map<String, Object> stats = new HashMap<>();
		try {
			String sql = """
                    SELECT 
                        COUNT(CASE WHEN last_login IS NOT NULL THEN 1 END) AS usersWithLogin,
                        COUNT(CASE WHEN last_login IS NULL THEN 1 END) AS usersWithoutLogin,
                        AVG(CASE WHEN last_login IS NOT NULL 
                            THEN EXTRACT(DAY FROM (NOW() - last_login)) ELSE NULL END) AS avgDaysSinceLastLogin,
                        MAX(last_login) AS lastLoginEver,
                        MIN(last_login) AS firstLoginEver
                    FROM users
                    """;

			Query query = entityManager.createNativeQuery(sql);
			Object[] result = (Object[]) query.getSingleResult();

			stats.put("usersWithLogin", getLongValue(result[0]));
			stats.put("usersWithoutLogin", getLongValue(result[1]));
			stats.put("avgDaysSinceLastLogin", getDoubleValue(result[2]));
			stats.put("lastLoginEver", result[3]);
			stats.put("firstLoginEver", result[4]);

		} catch (Exception e) {
			log.error("Error getting user activity statistics: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public List<Object[]> getMostActiveUsers(int limit) {
		try {
			String sql = """
                    SELECT 
                        u.user_id,
                        u.email,
                        u.first_name,
                        u.last_name,
                        COUNT(DISTINCT DATE(u.last_login)) AS loginDays
                    FROM users u
                    WHERE u.last_login IS NOT NULL
                    GROUP BY u.user_id, u.email, u.first_name, u.last_name
                    ORDER BY loginDays DESC
                    LIMIT :limit
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("limit", limit);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error getting most active users: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> getLeastActiveUsers(int limit) {
		try {
			String sql = """
                    SELECT 
                        u.user_id,
                        u.email,
                        u.first_name,
                        u.last_name,
                        u.last_login
                    FROM users u
                    WHERE u.is_active = true
                    ORDER BY u.last_login ASC NULLS FIRST
                    LIMIT :limit
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("limit", limit);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error getting least active users: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public double getAverageLoginFrequency() {
		try {
			String sql = """
                    SELECT AVG(login_count) 
                    FROM (
                        SELECT COUNT(*) AS login_count
                        FROM user_activity_log
                        GROUP BY user_id
                    ) AS login_stats
                    """;

			Query query = entityManager.createNativeQuery(sql);
			Double result = (Double) query.getSingleResult();
			return result != null ? result : 0.0;

		} catch (Exception e) {
			log.error("Error getting average login frequency: {}", e.getMessage(), e);
			return 0.0;
		}
	}

	@Override
	public Map<String, Object> getCategoryStatistics() {
		Map<String, Object> stats = new HashMap<>();
		try {
			List<Object[]> results = countGroupByCategory();
			stats.put("categories", results);
			stats.put("totalCategories", results.size());

			// Get category with most users
			if (!results.isEmpty()) {
				Object[] top = results.get(0);
				stats.put("topCategory", top[0]);
				stats.put("topCategoryCount", top[1]);
			}

		} catch (Exception e) {
			log.error("Error getting category statistics: {}", e.getMessage(), e);
		}
		return stats;
	}

	@Override
	public List<Object[]> getUsersByCategoryWithCounts() {
		try {
			String sql = """
                    SELECT 
                        uc.category_id,
                        uc.label,
                        uc.description,
                        COUNT(u.user_id) AS userCount,
                        SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END) AS activeCount,
                        SUM(CASE WHEN u.is_active = false THEN 1 ELSE 0 END) AS inactiveCount
                    FROM user_category uc
                    LEFT JOIN users u ON uc.category_id = u.category_id
                    GROUP BY uc.category_id, uc.label, uc.description
                    ORDER BY userCount DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error getting users by category with counts: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	@Transactional
	public void deactivateInactiveUsers(LocalDateTime thresholdDate) {
		try {
			String sql = """
                    UPDATE users 
                    SET is_active = false 
                    WHERE is_active = true 
                    AND last_login < :thresholdDate
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("thresholdDate", thresholdDate);
			int updated = query.executeUpdate();
			log.info("Deactivated {} inactive users", updated);

		} catch (Exception e) {
			log.error("Error deactivating inactive users: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to deactivate inactive users", e);
		}
	}

	@Override
	@Transactional
	public void activateUsersBulk(List<UUID> userIds) {
		try {
			if (userIds == null || userIds.isEmpty()) {
				return;
			}
			String sql = "UPDATE users SET is_active = true WHERE user_id IN (:userIds)";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userIds", userIds);
			int updated = query.executeUpdate();
			log.info("Activated {} users in bulk", updated);

		} catch (Exception e) {
			log.error("Error activating users in bulk: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to activate users in bulk", e);
		}
	}

	@Override
	@Transactional
	public void deactivateUsersBulk(List<UUID> userIds) {
		try {
			if (userIds == null || userIds.isEmpty()) {
				return;
			}
			String sql = "UPDATE users SET is_active = false WHERE user_id IN (:userIds)";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userIds", userIds);
			int updated = query.executeUpdate();
			log.info("Deactivated {} users in bulk", updated);

		} catch (Exception e) {
			log.error("Error deactivating users in bulk: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to deactivate users in bulk", e);
		}
	}

	@Override
	@Transactional
	public void deleteUsersBulk(List<UUID> userIds) {
		try {
			if (userIds == null || userIds.isEmpty()) {
				return;
			}
			String sql = "DELETE FROM users WHERE user_id IN (:userIds)";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("userIds", userIds);
			int deleted = query.executeUpdate();
			log.info("Deleted {} users in bulk", deleted);

		} catch (Exception e) {
			log.error("Error deleting users in bulk: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to delete users in bulk", e);
		}
	}

	@Override
	public List<Object[]> findUsersWithNoActivity() {
		try {
			String sql = """
                    SELECT 
                        u.user_id,
                        u.email,
                        u.first_name,
                        u.last_name,
                        u.created_at
                    FROM users u
                    WHERE u.last_login IS NULL
                    AND u.is_active = true
                    ORDER BY u.created_at DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error finding users with no activity: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public List<Object[]> findUsersWithRecentActivity(int days) {
		try {
			LocalDateTime threshold = LocalDateTime.now().minusDays(days);
			String sql = """
                    SELECT 
                        u.user_id,
                        u.email,
                        u.first_name,
                        u.last_name,
                        u.last_login
                    FROM users u
                    WHERE u.last_login > :threshold
                    AND u.is_active = true
                    ORDER BY u.last_login DESC
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("threshold", threshold);
			return query.getResultList();

		} catch (Exception e) {
			log.error("Error finding users with recent activity: {}", e.getMessage(), e);
			return List.of();
		}
	}

	@Override
	public long countUsersWithNoLogin() {
		try {
			String sql = "SELECT COUNT(u.user_id) FROM users u WHERE u.last_login IS NULL AND u.is_active = true";
			Query query = entityManager.createNativeQuery(sql);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users with no login: {}", e.getMessage(), e);
			return 0L;
		}
	}

	@Override
	public long countUsersWithLoginInLastDays(int days) {
		try {
			LocalDateTime threshold = LocalDateTime.now().minusDays(days);
			String sql = """
                    SELECT COUNT(u.user_id) 
                    FROM users u 
                    WHERE u.last_login > :threshold AND u.is_active = true
                    """;

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("threshold", threshold);
			Long result = (Long) query.getSingleResult();
			return result != null ? result : 0L;

		} catch (Exception e) {
			log.error("Error counting users with login in last days: {}", e.getMessage(), e);
			return 0L;
		}
	}

	private Long getLongValue(Object value) {
		if (value == null) return 0L;
		if (value instanceof Long) return (Long) value;
		if (value instanceof Integer) return ((Integer) value).longValue();
		if (value instanceof Number) return ((Number) value).longValue();
		return 0L;
	}

	private Double getDoubleValue(Object value) {
		return switch (value) {
			case Double v -> v;
			case Number number -> number.doubleValue();
			case null, default -> 0.0;
		};
	}
}