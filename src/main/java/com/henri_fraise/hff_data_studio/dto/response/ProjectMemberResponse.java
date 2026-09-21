package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {
	private UUID memberId;
	private UUID projectId;
	private UUID userId;
	private String userFullName;
	private String userEmail;
	private MemberRole role;
	private LocalDateTime joinedAt;
	private String invitedByName;
}