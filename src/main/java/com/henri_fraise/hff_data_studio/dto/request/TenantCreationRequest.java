package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreationRequest {

	@NotBlank(message = "Code is required")
	private String code;

	@NotBlank(message = "Name is required")
	private String name;

	private String domain;

	private Integer maxUsers;

	private Long maxStorageMb;
}