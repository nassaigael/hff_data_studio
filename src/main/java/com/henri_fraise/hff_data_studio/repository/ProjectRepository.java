package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Project;

import java.util.List;
import java.util.UUID;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
  Page<Project> findByCreator_Id(UUID userId, Pageable pageable);

  Page<Project> findByCreator_IdAndStatus(UUID userId, ProjectStatus status, Pageable pageable);

  List<Project> findByCreator_IdAndStatus(UUID userId, ProjectStatus status);

  Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

  List<Project> findByStatus(ProjectStatus status);


  boolean existsByProjectName(String projectName);

  boolean existsByProjectNameAndCreator_Id(String projectName, UUID userId);

  boolean existsByProjectNameAndIdNot(String projectName, UUID id);

  long countByCreator_Id(UUID userId);

  long countByStatus(ProjectStatus status);

  long countByCreator_IdAndStatus(UUID userId, ProjectStatus status);



}
