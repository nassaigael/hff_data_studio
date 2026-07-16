package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.ProjectUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.ProjectMapper;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final CustomProjectRepository customProjectRepository;
	private final ProjectMapper projectMapper;
	private final UserService userService;
	private final AuditLogService auditLogService;

	// ==================== CRUD Operations ====================

	public Page<ProjectResponse> getAllProjects(Pageable pageable) {
		try {
			Page<Project> projects = projectRepository.findAll(pageable);
			return projects.map(projectMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving projects: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve projects", ex);
		}
	}

	public Page<ProjectResponse> getUserProjects(UUID userId, Pageable pageable) {
		try {
			// Verify user exists
			userService.getUserEntityById(userId);

			Page<Project> projects = projectRepository.findByCreatorId(userId, pageable);
			return projects.map(projectMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving user projects: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve user projects", ex);
		}
	}

	public Page<ProjectResponse> getUserProjectsByStatus(UUID userId, ProjectStatus status, Pageable pageable) {
		try {
			userService.getUserEntityById(userId);
			Page<Project> projects = projectRepository.findByCreatorIdAndStatus(userId, status, pageable);
			return projects.map(projectMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving user projects by status: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve user projects by status", ex);
		}
	}

	public ProjectResponse getProjectById(UUID projectId) {
		Project project = getProjectEntityById(projectId);
		return projectMapper.toResponse(project);
	}

	public Project getProjectEntityById(UUID projectId) {
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
	}

	@Transactional
	public ProjectResponse createProject(ProjectCreationRequest request, UUID userId) {
		User user = userService.getUserEntityById(userId);

		// Check if project name already exists for this user
		if (projectRepository.existsByProjectNameAndCreatorId(request.getProjectName(), userId)) {
			throw new ResourceAlreadyExistsException("Project", "name", request.getProjectName());
		}

		try {
			Project project = projectMapper.toEntity(request, user);
			Project saved = projectRepository.save(project);

			log.info("Project created successfully: {} ({}) by user {}",
					saved.getProjectName(), saved.getId(), userId);

			// Audit log
			auditLogService.logAction(
					"PROJECT_CREATED",
					"Project",
					saved.getId(),
					"Project " + saved.getProjectName() + " created by " + user.getEmail()
			);

			return projectMapper.toResponse(saved);
		} catch (Exception ex) {
			log.error("Error creating project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create project", ex);
		}
	}

	@Transactional
	public ProjectResponse updateProject(UUID projectId, ProjectUpdateRequest request, UUID userId) {
		Project project = getProjectEntityById(projectId);

		// Verify user owns the project
		if (!project.getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to update this project");
		}

		// Check name uniqueness if changed
		if (request.getProjectName() != null && !request.getProjectName().equals(project.getProjectName())) {
			if (projectRepository.existsByProjectNameAndCreatorId(request.getProjectName(), userId)) {
				throw new ResourceAlreadyExistsException("Project", "name", request.getProjectName());
			}
		}

		try {
			projectMapper.updateEntity(project, request);
			Project updated = projectRepository.save(project);

			log.info("Project updated successfully: {} ({})", updated.getProjectName(), updated.getId());

			// Audit log
			auditLogService.logAction(
					"PROJECT_UPDATED",
					"Project",
					projectId,
					"Project " + updated.getProjectName() + " updated"
			);

			return projectMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update project", ex);
		}
	}

	@Transactional
	public void archiveProject(UUID projectId, UUID userId) {
		Project project = getProjectEntityById(projectId);

		// Verify user owns the project
		if (!project.getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to archive this project");
		}

		if (project.getStatus() == ProjectStatus.ARCHIVED) {
			throw new ValidationException("Project is already archived");
		}

		try {
			project.setStatus(ProjectStatus.ARCHIVED);
			projectRepository.save(project);

			log.info("Project archived successfully: {} ({})", project.getProjectName(), projectId);

			// Audit log
			auditLogService.logAction(
					"PROJECT_ARCHIVED",
					"Project",
					projectId,
					"Project " + project.getProjectName() + " archived by " + userId
			);
		} catch (Exception ex) {
			log.error("Error archiving project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to archive project", ex);
		}
	}

	@Transactional
	public void restoreProject(UUID projectId, UUID userId) {
		Project project = getProjectEntityById(projectId);

		// Verify user owns the project
		if (!project.getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to restore this project");
		}

		if (project.getStatus() != ProjectStatus.ARCHIVED) {
			throw new ValidationException("Project is not archived");
		}

		try {
			project.setStatus(ProjectStatus.IN_PROGRESS);
			projectRepository.save(project);

			log.info("Project restored successfully: {} ({})", project.getProjectName(), projectId);

			// Audit log
			auditLogService.logAction(
					"PROJECT_RESTORED",
					"Project",
					projectId,
					"Project " + project.getProjectName() + " restored by " + userId
			);
		} catch (Exception ex) {
			log.error("Error restoring project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to restore project", ex);
		}
	}

	@Transactional
	public void deleteProject(UUID projectId, UUID userId) {
		Project project = getProjectEntityById(projectId);

		// Verify user owns the project or is admin
		if (!project.getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to delete this project");
		}

		// Check if project has files
		if (!project.getSourceFiles().isEmpty()) {
			throw new ValidationException("Cannot delete project with existing files. Please delete files first.");
		}

		try {
			projectRepository.delete(project);
			log.info("Project deleted successfully: {} ({})", project.getProjectName(), projectId);

			// Audit log
			auditLogService.logAction(
					"PROJECT_DELETED",
					"Project",
					projectId,
					"Project " + project.getProjectName() + " deleted by " + userId
			);
		} catch (Exception ex) {
			log.error("Error deleting project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete project", ex);
		}
	}

	// ==================== Search Operations ====================

	public Page<ProjectResponse> searchUserProjects(UUID userId, String searchTerm, Pageable pageable) {
		try {
			userService.getUserEntityById(userId);
			Page<Project> projects = projectRepository.searchUserProjects(userId, searchTerm, pageable);
			return projects.map(projectMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error searching user projects: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search user projects", ex);
		}
	}

	public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
		try {
			Page<Project> projects = projectRepository.findByStatus(status, pageable);
			return projects.map(projectMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving projects by status: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve projects by status", ex);
		}
	}

	// ==================== Statistics Operations ====================

	public ProjectStatisticsResponse getProjectStatistics() {
		try {
			return customProjectRepository.getProjectStatistics();
		} catch (Exception ex) {
			log.error("Error retrieving project statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve project statistics", ex);
		}
	}

	public long countProjectsByUser(UUID userId) {
		return projectRepository.countByCreatorId(userId);
	}

	public long countProjectsByUserAndStatus(UUID userId, ProjectStatus status) {
		return projectRepository.countByCreatorIdAndStatus(userId, status);
	}

	// ==================== Validation Methods ====================

	public boolean existsByProjectName(String projectName) {
		return projectRepository.existsByProjectName(projectName);
	}

	public boolean existsByProjectNameAndUser(String projectName, UUID userId) {
		return projectRepository.existsByProjectNameAndCreatorId(projectName, userId);
	}

	// ==================== Maintenance Operations ====================

	@Transactional
	public void archiveInactiveProjects() {
		LocalDateTime olderThan = LocalDateTime.now().minusMonths(6);
		try {
			customProjectRepository.archiveInactiveProjects(olderThan);
			log.info("Archived inactive projects older than 6 months");
		} catch (Exception ex) {
			log.error("Error archiving inactive projects: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to archive inactive projects", ex);
		}
	}
}