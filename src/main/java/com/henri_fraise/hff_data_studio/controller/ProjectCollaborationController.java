package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ShareProjectRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectMemberResponse;
import com.henri_fraise.hff_data_studio.enums.MemberRole;
import com.henri_fraise.hff_data_studio.service.ProjectCollaborationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collaboration/projects")
@RequiredArgsConstructor
public class ProjectCollaborationController {

	private final ProjectCollaborationService collaborationService;

	@PostMapping("/share")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProjectMemberResponse> share(
			@Valid @RequestBody ShareProjectRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(collaborationService.shareProject(request));
	}

	@GetMapping("/{projectId}/members")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ProjectMemberResponse>> list(@PathVariable UUID projectId) {
		return ResponseEntity.ok(collaborationService.listMembers(projectId));
	}

	@PatchMapping("/{projectId}/members/{userId}/role")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> updateRole(@PathVariable UUID projectId,
	                                       @PathVariable UUID userId,
	                                       @RequestParam MemberRole role) {
		collaborationService.updateMemberRole(projectId, userId, role);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{projectId}/members/{userId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> revoke(@PathVariable UUID projectId, @PathVariable UUID userId) {
		collaborationService.revokeAccess(projectId, userId);
		return ResponseEntity.noContent().build();
	}
}