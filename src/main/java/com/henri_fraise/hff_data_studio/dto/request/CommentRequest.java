package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

	@NotBlank(message = "Content is required")
	@Size(max = 5000)
	private String content;

	@NotBlank(message = "Entity type is required")
	private String entityType;

	@NotNull(message = "Entity ID is required")
	private UUID entityId;

	private UUID parentId;
}