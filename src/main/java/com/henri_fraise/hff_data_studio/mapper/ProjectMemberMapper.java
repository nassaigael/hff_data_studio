package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ProjectMemberResponse;
import com.henri_fraise.hff_data_studio.entity.ProjectMember;
import org.springframework.stereotype.Component;

@Component
public class ProjectMemberMapper {

	public ProjectMemberResponse toResponse(ProjectMember member) {
		if (member == null) return null;

		return ProjectMemberResponse.builder()
				.memberId(member.getId())
				.projectId(member.getProject() != null ? member.getProject().getId() : null)
				.userId(member.getUser() != null ? member.getUser().getId() : null)
				.userFullName(member.getUser() != null
						? member.getUser().getFirstName() + " " + member.getUser().getLastName()
						: null)
				.userEmail(member.getUser() != null ? member.getUser().getEmail() : null)
				.role(member.getRole())
				.joinedAt(member.getJoinedAt())
				.invitedByName(member.getInvitedBy() != null
						? member.getInvitedBy().getFirstName() + " " + member.getInvitedBy().getLastName()
						: null)
				.build();
	}
}