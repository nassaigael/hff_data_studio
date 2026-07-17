package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

  Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

  boolean existsByProjectName(String projectName);

  boolean existsByProjectNameAndCreator_Id(String projectName, UUID userId);

  boolean existsByProjectNameAndIdNot(String projectName, UUID id);

  long countByStatus(ProjectStatus status);

  @Modifying
  @Transactional
  @Query("UPDATE Project p SET p.status = :status WHERE p.id = :project_id")
  void updateProjectStatus(
      @Param("project_id") UUID projectId, @Param("status") ProjectStatus status);

  @Modifying
  @Transactional
  @Query("UPDATE Project p SET p.status = :status WHERE p.creator.id = :user_Id")
  void updateAllProjectsStatusForUser(
      @Param("user_Id") UUID userId, @Param("status") ProjectStatus status);

  @Query(
      "SELECT p FROM Project  p WHERE p.creator.id = :user_id AND" +
              ":searchTerm IS NULL OR"
          + " LOWER(p.projectName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description)"
          + " LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<Project> searchUserProjects(
      @Param("user_id") UUID userId, @Param("searchTerm") String searchTerm, Pageable pageable);

  @Query("SELECT p FROM Project  p WHERE p.createdAt BETWEEN :start_date AND :end_date")
  List<Project> findProjectsCreatedBetween(
      @Param("start_date") String startDate, @Param("end_date") String endDate);

  @Query("SELECT p  FROM Project p WHERE p.status = :status AND p.createdAt < :date")
  List<Project> findProjectsByStatusAndOlderThan(
      @Param("status") ProjectStatus status, @Param("date") String date);

  boolean existsByProjectNameAndCreatorId(String projectName, UUID creatorId);


  long countByCreatorIdAndStatus(UUID creatorId, ProjectStatus status);

  long countByCreatorId(UUID userId);

  Page<Project> findByCreatorId(UUID userId, Pageable pageable);

  Page<Project> findByCreatorIdAndStatus(UUID userId, ProjectStatus status, Pageable pageable);

  @Query("SELECT COUNT(p) FROM Project p WHERE p.createdAt BETWEEN :start_date AND :end_date")
  long countProjectsCreatedBetween(@Param("start_date") LocalDateTime startDate, @Param("end_date") LocalDateTime endDate);

}
