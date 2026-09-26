package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectTemplateResponse;
import com.henri_fraise.hff_data_studio.service.ProjectTemplateService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class ProjectTemplateController {

	private final ProjectTemplateService templateService;

	@PostMapping("/from-project/{projectId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProjectTemplateResponse> createFromProject(
			@PathVariable UUID projectId,
			@RequestBody Map<String, Object> body) {
		return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createFromProject(
				projectId,
				(String) body.get("name"),
				(String) body.get("description"),
				(String) body.get("category"),
				Boolean.TRUE.equals(body.get("isPublic"))));
	}

	@PostMapping("/{templateId}/instantiate")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProjectResponse> instantiate(
			@PathVariable UUID templateId,
			@RequestParam String projectName) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(templateService.instantiate(templateId, projectName));
	}

	@GetMapping("/mine")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ProjectTemplateResponse>> listMine() {
		return ResponseEntity.ok(templateService.listMine());
	}

	@GetMapping("/public")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ProjectTemplateResponse>> listPublic() {
		return ResponseEntity.ok(templateService.listPublic());
	}

	@DeleteMapping("/{templateId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID templateId) {
		templateService.delete(templateId);
		return ResponseEntity.noContent().build();
	}
}