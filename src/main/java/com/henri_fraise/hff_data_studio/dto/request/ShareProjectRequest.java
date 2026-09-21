package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareProjectRequest {

	@NotNull(message = "Project ID is required")
	private java.util.UUID projectId;

	@Email(message = "Email must be valid")
	private String userEmail;

	private java.util.UUID userId;

	@NotNull(message = "Role is required")
	private MemberRole role;
}