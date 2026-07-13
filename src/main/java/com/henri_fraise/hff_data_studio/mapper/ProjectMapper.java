package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {
	public ProjectResponse toResponse(Project project) {
		if (project == null)
			return null;

		return ProjectResponse.builder()
				.projectId(project.getId())
				.projectName(project.getProjectName())
				.description(project.getDescription())
				.createdAt(project.getCreatedAt())
				.status(project.getStatus())
				.creatorUserId(
						project.getCreator() != null
								? project.getCreator().getId()
								: null
				)
				.creatorFullName(
						project.getCreator() != null
								? project.getCreator().getFirstName() + " " + project.getCreator().getLastName()
								: null
				)
				.fileCount(
						project.getSourceFiles() != null ? (long) project.getSourceFiles().size() : 0L
				)
				.datasetCount(
						project.getSourceFiles() != null
								? project.getSourceFiles().stream()
								.mapToLong(f -> f.getDatasets() != null ? f.getDatasets().size() : 0)
								.sum()
								: 0L
				)
				.build();
	}

	public Project toEntity(ProjectCreationRequest request, User creator) {
		if (request == null)
			return null;

		return Project.builder()
				.projectName(request.getProjectName())
				.description(request.getDescription())
				.status(ProjectStatus.IN_PROGRESS)
				.creator(creator)
				.build();
	}

	public void updateEntity(Project project, ProjectCreationRequest request) {
		if (request == null || project == null)
			return;
		if (request.getProjectName() != null)
			project.setProjectName(request.getProjectName());
		if (request.getDescription() != null)
			project.setDescription(request.getDescription());
		if (request.getStatus() != null)
			project.setStatus(request.getStatus());
	}
}
