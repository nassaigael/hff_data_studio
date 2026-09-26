package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class CustomProjectRepositoryImpl implements CustomProjectRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public ProjectStatisticsResponse getProjectStatistics() {
    try {
      String sql =
              "SELECT "
                      + "COUNT(*) as total_projects, "
                      + "COALESCE(SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END), 0) as in_progress, "
                      + "COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END), 0) as completed, "
                      + "COALESCE(SUM(CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END), 0) as archived, "
                      + "COALESCE((SELECT COUNT(DISTINCT creator_user_id) FROM project), 0) as total_creators, "
                      + "COALESCE((SELECT COUNT(*) FROM project WHERE created_at >= NOW() - INTERVAL '30 days'), 0) as new_projects_last_30_days "
                      + "FROM project";

      Query query = entityManager.createNativeQuery(sql);
      Object[] result = (Object[]) query.getSingleResult();

      return ProjectStatisticsResponse.builder()
              .totalProjects(getLongValue(result[0]))
              .inProgress(getLongValue(result[1]))
              .completed(getLongValue(result[2]))
              .archived(getLongValue(result[3]))
              .totalCreators(getLongValue(result[4]))
              .newProjectsLast30Days(getLongValue(result[5]))
              .build();

    } catch (Exception e) {
      log.error("Error getting project statistics: {}", e.getMessage(), e);
      return ProjectStatisticsResponse.builder()
              .totalProjects(0L)
              .inProgress(0L)
              .completed(0L)
              .archived(0L)
              .totalCreators(0L)
              .newProjectsLast30Days(0L)
              .build();
    }
  }

  @Override
  public long countProjectsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    try {
      Query query =
              entityManager.createQuery(
                      "SELECT COUNT(p) FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate");
      query.setParameter("startDate", startDate);
      query.setParameter("endDate", endDate);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting projects by date range: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  public long countActiveProjectsByUser(UUID userId) {
    try {
      Query query =
              entityManager.createQuery(
                      "SELECT COUNT(p) FROM Project p WHERE p.creator.id = :userId AND p.status <> 'ARCHIVED'");
      query.setParameter("userId", userId);
      return getLongValue(query.getSingleResult());
    } catch (Exception e) {
      log.error("Error counting active projects by user: {}", e.getMessage(), e);
      return 0L;
    }
  }

  @Override
  @Transactional
  public void archiveInactiveProjects(LocalDateTime olderThan) {
    try {
      Query query =
              entityManager.createQuery(
                      "UPDATE Project p SET p.status = 'ARCHIVED' "
                              + "WHERE p.status = 'COMPLETED' AND p.createdAt < :olderThan");
      query.setParameter("olderThan", olderThan);
      int updated = query.executeUpdate();
      log.info("Archived {} inactive projects", updated);
    } catch (Exception e) {
      log.error("Error archiving inactive projects: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to archive inactive projects", e);
    }
  }

  @Override
  @Transactional
  public void archiveProjectsByUser(UUID userId) {
    try {
      Query query =
              entityManager.createQuery(
                      "UPDATE Project p SET p.status = 'ARCHIVED' WHERE p.creator.id = :userId");
      query.setParameter("userId", userId);
      int updated = query.executeUpdate();
      log.info("Archived {} projects for user: {}", updated, userId);
    } catch (Exception e) {
      log.error("Error archiving projects by user: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to archive projects by user", e);
    }
  }

  private Long getLongValue(Object value) {
    if (value == null) return 0L;
    if (value instanceof Long) return (Long) value;
    if (value instanceof Integer) return ((Integer) value).longValue();
    if (value instanceof BigInteger) return ((BigInteger) value).longValue();
    if (value instanceof BigDecimal) return ((BigDecimal) value).longValue();
    if (value instanceof Number) return ((Number) value).longValue();
    return 0L;
  }

  private Double getDoubleValue(Object value) {
    if (value == null) return 0.0;
    if (value instanceof Double) return (Double) value;
    if (value instanceof Float) return ((Float) value).doubleValue();
    if (value instanceof Integer) return ((Integer) value).doubleValue();
    if (value instanceof Long) return ((Long) value).doubleValue();
    if (value instanceof BigInteger) return ((BigInteger) value).doubleValue();
    if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
    if (value instanceof Number) return ((Number) value).doubleValue();
    return 0.0;
  }
}