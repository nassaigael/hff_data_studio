package com.henri_fraise.hff_data_studio.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChangePasswordRequest {
	@NotNull(message = "Old password is required")
	private String newPassword;
}
