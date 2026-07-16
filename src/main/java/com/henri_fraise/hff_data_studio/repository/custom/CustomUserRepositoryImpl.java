package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public UserStatisticsResponse getUserStatistics() {
		Query query = entityManager.createNativeQuery(
				"SELECT " +
						"COUNT(*) as total_users, " +
						"SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_users, " +
						"SUM(CASE WHEN is_active = false THEN 1 ELSE 0 END) as inactive_users, " +
						"COUNT(DISTINCT category_id) as total_categories, " +
						"(SELECT COUNT(*) FROM "user" WHERE created_at >= NOW() - INTERVAL '30 days') as new_users_last_30_days, " +
						"(SELECT COUNT(*) FROM "user" WHERE created_at >= NOW() - INTERVAL '7 days') as new_users_last_7_days, " +
						"(SELECT COUNT(*) FROM "user" WHERE last_login >= NOW() - INTERVAL '30 days') as active_users_last_30_days, " +
						"(SELECT COUNT(*) FROM "user" WHERE last_login >= NOW() - INTERVAL '7 days') as active_users_last_7_days " +
						"FROM "user""
		);

		Object[] result = (Object[]) query.getSingleResult();

		return UserStatisticsResponse.builder()
				.totalUsers(((Number) result[0]).longValue())
				.activeUsers(((Number) result[1]).longValue())
				.inactiveUsers(((Number) result[2]).longValue())
				.totalCategories(((Number) result[3]).longValue())
				.newUsersLast30Days(((Number) result[4]).longValue())
				.newUsersLast7Days(((Number) result[5]).longValue())
				.activeUsersLast30Days(((Number) result[6]).longValue())
				.activeUsersLast7Days(((Number) result[7]).longValue())
				.build();
	}

	@Override
	public long countActiveUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.createdAt BETWEEN :startDate AND :endDate"
		);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	public long countNewUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate"
		);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	@Transactional
	public void updateUserActivity(UUID userId, String activity) {
		Query query = entityManager.createNativeQuery(
				"INSERT INTO user_activity (user_id, activity, activity_date) VALUES (:userId, :activity, NOW())"
		);
		query.setParameter("userId", userId);
		query.setParameter("activity", activity);
		query.executeUpdate();
	}
}