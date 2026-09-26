package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.ShareProjectRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectMemberResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.ProjectMember;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.MemberRole;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.ProjectMemberMapper;
import com.henri_fraise.hff_data_studio.repository.ProjectMemberRepository;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCollaborationService {

	private final ProjectMemberRepository memberRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final ProjectMemberMapper memberMapper;
	private final NotificationService notificationService;
	private final SecurityUtils securityUtils;
	private final AuditLogService auditLogService;

	@Transactional
	public ProjectMemberResponse shareProject(ShareProjectRequest request) {
		Project project = projectRepository.findById(request.getProjectId())
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		User targetUser = resolveTargetUser(request);
		User currentUser = securityUtils.getCurrentUser();
		validateOwnership(project, currentUser);

		if (memberRepository.existsByProjectIdAndUserId(project.getId(), targetUser.getId())) {
			throw new ResourceAlreadyExistsException("User already has access to this project");
		}

		ProjectMember member = ProjectMember.builder()
				.project(project)
				.user(targetUser)
				.role(request.getRole())
				.invitedBy(currentUser)
				.build();

		ProjectMember saved = memberRepository.save(member);

		notificationService.send(
				targetUser.getId(),
				NotificationType.INFO,
				"Project shared",
				currentUser.getFullName() + " shared project '" + project.getProjectName() + "' with you",
				"/projects/" + project.getId());

		auditLogService.logAction("PROJECT_SHARED", "Project", project.getId(),
				"Shared with " + targetUser.getEmail());

		return memberMapper.toResponse(saved);
	}

	@Transactional
	public void updateMemberRole(UUID projectId, UUID userId, MemberRole role) {
		ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));
		Project project = member.getProject();
		validateOwnership(project, securityUtils.getCurrentUser());

		member.setRole(role);
		memberRepository.save(member);
	}

	@Transactional
	public void revokeAccess(UUID projectId, UUID userId) {
		ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found"));
		validateOwnership(member.getProject(), securityUtils.getCurrentUser());
		memberRepository.delete(member);
	}

	@Transactional(readOnly = true)
	public List<ProjectMemberResponse> listMembers(UUID projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		validateAccess(project, securityUtils.getCurrentUser());
		return memberRepository.findByProjectId(projectId).stream()
				.map(memberMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public boolean hasAccess(UUID projectId, UUID userId) {
		Project project = projectRepository.findById(projectId).orElse(null);
		if (project == null) return false;
		if (project.getCreator().getId().equals(userId)) return true;
		return memberRepository.existsByProjectIdAndUserId(projectId, userId);
	}

	private User resolveTargetUser(ShareProjectRequest request) {
		if (request.getUserId() != null) {
			return userRepository.findById(request.getUserId())
					.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		}
		return userRepository.findByEmail(request.getUserEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private void validateOwnership(Project project, User user) {
		if (!project.getCreator().getId().equals(user.getId())
				&& !securityUtils.isAdmin()) {
			throw new com.henri_fraise.hff_data_studio.exception.ForbiddenException(
					"Only the project owner can perform this action");
		}
	}

	private void validateAccess(Project project, User user) {
		if (!project.getCreator().getId().equals(user.getId())
				&& !memberRepository.existsByProjectIdAndUserId(project.getId(), user.getId())
				&& !securityUtils.isAdmin()) {
			throw new com.henri_fraise.hff_data_studio.exception.ForbiddenException(
					"You do not have access to this project");
		}
	}
}