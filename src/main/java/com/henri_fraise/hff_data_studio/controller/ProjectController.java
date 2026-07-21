package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.ProjectUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.ProjectService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;
  private final PageMapper pageMapper;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PageResponse<ProjectResponse>> getAllProjects(
      @PageableDefault(size = 20) Pageable pageable,
      @RequestParam(required = false) ProjectStatus status) {
    UUID userId = securityUtils.getCurrentUserId();
    Page<ProjectResponse> projects = projectService.getUserProjects(userId, pageable, status);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/all")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<PageResponse<ProjectResponse>> getAllProjectsAdmin(
      @PageableDefault(size = 20) Pageable pageable,
      @RequestParam(required = false) ProjectStatus status) {
    Page<ProjectResponse> projects = projectService.getAllProjects(pageable, status);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/{projectId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID projectId) {
    UUID userId = securityUtils.getCurrentUserId();
    return ResponseEntity.ok(projectService.getProjectById(projectId, userId));
  }

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProjectResponse> createProject(
      @Valid @RequestBody ProjectCreationRequest request) {
    UUID userId = securityUtils.getCurrentUserId();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.createProject(request, userId));
  }

  @PutMapping("/{projectId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProjectResponse> updateProject(
      @PathVariable UUID projectId, @Valid @RequestBody ProjectUpdateRequest request) {
    UUID userId = securityUtils.getCurrentUserId();
    return ResponseEntity.ok(projectService.updateProject(projectId, request, userId));
  }

  @DeleteMapping("/{projectId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> archiveProject(@PathVariable UUID projectId) {
    UUID userId = securityUtils.getCurrentUserId();
    projectService.archiveProject(projectId, userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{projectId}/restore")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProjectResponse> restoreProject(@PathVariable UUID projectId) {
    UUID userId = securityUtils.getCurrentUserId();
    return ResponseEntity.ok(projectService.restoreProject(projectId, userId));
  }

  @PatchMapping("/{projectId}/status")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProjectResponse> updateProjectStatus(
      @PathVariable UUID projectId, @RequestParam ProjectStatus status) {
    UUID userId = securityUtils.getCurrentUserId();
    return ResponseEntity.ok(projectService.updateProjectStatus(projectId, status, userId));
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PageResponse<ProjectResponse>> getProjectsByUser(
      @PathVariable UUID userId, @PageableDefault(size = 20) Pageable pageable) {
    Page<ProjectResponse> projects = projectService.getUserProjects(userId, pageable, null);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/user/{userId}/status/{status}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PageResponse<ProjectResponse>> getProjectsByUserAndStatus(
      @PathVariable UUID userId,
      @PathVariable ProjectStatus status,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<ProjectResponse> projects =
        projectService.getUserProjectsByStatus(userId, status, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/search")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PageResponse<ProjectResponse>> searchProjects(
      @RequestParam String searchTerm, @PageableDefault(size = 20) Pageable pageable) {
    UUID userId = securityUtils.getCurrentUserId();
    Page<ProjectResponse> projects =
        projectService.searchUserProjects(userId, searchTerm, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<PageResponse<ProjectResponse>> getProjectsByStatus(
      @PathVariable ProjectStatus status, @PageableDefault(size = 20) Pageable pageable) {
    Page<ProjectResponse> projects = projectService.getProjectsByStatus(status, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(projects, project -> project));
  }

  @GetMapping("/statistics")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<ProjectStatisticsResponse> getProjectStatistics() {
    return ResponseEntity.ok(projectService.getProjectStatistics());
  }

  @DeleteMapping("/{projectId}/hard")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> hardDeleteProject(@PathVariable UUID projectId) {
    UUID userId = securityUtils.getCurrentUserId();
    projectService.deleteProject(projectId, userId);
    return ResponseEntity.noContent().build();
  }
}
