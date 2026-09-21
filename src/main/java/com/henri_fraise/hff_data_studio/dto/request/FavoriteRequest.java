package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {

	@NotBlank(message = "Entity type is required")
	private String entityType;

	@NotNull(message = "Entity ID is required")
	private UUID entityId;

	private String displayName;
}