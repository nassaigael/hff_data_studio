package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.TagColor;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {

	@NotBlank(message = "Tag name is required")
	private String name;

	private TagColor color;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TagAssignRequest {
		private String entityType;
		private UUID entityId;
		private List<UUID> tagIds;
	}
}