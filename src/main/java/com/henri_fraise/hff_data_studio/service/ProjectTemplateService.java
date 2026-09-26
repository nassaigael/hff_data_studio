package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectTemplateResponse;
import com.henri_fraise.hff_data_studio.entity.ProjectTemplate;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.ProjectTemplateRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.ProjectService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTemplateService {

	private final ProjectTemplateRepository templateRepository;
	private final ProjectService projectService;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;

	@Transactional
	public ProjectTemplateResponse createFromProject(UUID projectId, String name,
	                                                 String description, String category,
	                                                 boolean isPublic) {
		ProjectResponse project = projectService.getProjectById(projectId);
		Map<String, Object> config = new HashMap<>();
		config.put("projectName", project.getProjectName());
		config.put("description", project.getDescription());

		ProjectTemplate template = ProjectTemplate.builder()
				.name(name)
				.description(description)
				.category(category)
				.configJson(serialize(config))
				.isPublic(isPublic)
				.user(securityUtils.getCurrentUser())
				.build();

		return toResponse(templateRepository.save(template));
	}

	@Transactional
	public ProjectResponse instantiate(UUID templateId, String projectName) {
		ProjectTemplate template = templateRepository.findById(templateId)
				.orElseThrow(() -> new ResourceNotFoundException("Template not found"));

		ProjectCreationRequest request = new ProjectCreationRequest();
		request.setProjectName(projectName);
		request.setDescription(template.getDescription());

		ProjectResponse created = projectService.createProject(
				request, securityUtils.getCurrentUserId());

		template.setUsageCount(template.getUsageCount() + 1);
		templateRepository.save(template);

		return created;
	}

	@Transactional(readOnly = true)
	public List<ProjectTemplateResponse> listMine() {
		return templateRepository.findByUserIdOrderByUsageCountDesc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<ProjectTemplateResponse> listPublic() {
		return templateRepository.findByIsPublicTrueOrderByUsageCountDesc()
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void delete(UUID templateId) {
		templateRepository.delete(templateRepository.findById(templateId)
				.orElseThrow(() -> new ResourceNotFoundException("Template not found")));
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			return "{}";
		}
	}

	private Map<String, Object> deserialize(String json) {
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			return Map.of();
		}
	}

	private ProjectTemplateResponse toResponse(ProjectTemplate t) {
		return ProjectTemplateResponse.builder()
				.templateId(t.getId())
				.name(t.getName())
				.description(t.getDescription())
				.category(t.getCategory())
				.config(deserialize(t.getConfigJson()))
				.isPublic(t.getIsPublic())
				.usageCount(t.getUsageCount())
				.createdAt(t.getCreatedAt())
				.build();
	}
}