package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomProjectRepositoryImpl implements CustomProjectRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public ProjectStatisticsResponse getProjectStatistics() {
		Query query = entityManager.createNativeQuery(
				"SELECT " +
						"COUNT(*) as total_projects, " +
						"SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress, " +
						"SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
						"SUM(CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END) as archived, " +
						"(SELECT COUNT(DISTINCT creator_user_id) FROM project) as total_creators, " +
						"(SELECT COUNT(*) FROM project WHERE created_at >= NOW() - INTERVAL '30 days') as new_projects_last_30_days " +
						"FROM project"
		);

		Object[] result = (Object[]) query.getSingleResult();

		return ProjectStatisticsResponse.builder()
				.totalProjects(((Number) result[0]).longValue())
				.inProgress(((Number) result[1]).longValue())
				.completed(((Number) result[2]).longValue())
				.archived(((Number) result[3]).longValue())
				.totalCreators(((Number) result[4]).longValue())
				.newProjectsLast30Days(((Number) result[5]).longValue())
				.build();
	}

	@Override
	public long countProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(p) FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate"
		);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	public long countActiveProjectsByUser(UUID userId) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(p) FROM Project p WHERE p.creator.id = :userId AND p.status != 'ARCHIVED'"
		);
		query.setParameter("userId", userId);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	@Transactional
	public void archiveInactiveProjects(LocalDateTime olderThan) {
		Query query = entityManager.createQuery(
				"UPDATE Project p SET p.status = 'ARCHIVED' " +
						"WHERE p.status = 'COMPLETED' AND p.createdAt < :olderThan"
		);
		query.setParameter("olderThan", olderThan);
		query.executeUpdate();
	}

	@Override
	public void archiveProjectByUserId(UUID userId) {
		Query query = entityManager.createQuery(
				"UPDATE Project p SET p.status = 'ARCHIVED' " +
						"WHERE p.creator.id = : user_id"
		);

		query.setParameter("user_id", userId);
		query.executeUpdate();
	}
}