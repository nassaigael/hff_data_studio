package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.WidgetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRequest {

	@NotBlank(message = "Name is required")
	private String name;

	private String description;

	private Boolean isDefault;

	private Boolean isPublic;

	private UUID projectId;

	private List<WidgetRequest> widgets;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WidgetRequest {
		@NotNull private WidgetType type;
		@NotBlank private String title;
		private Integer position;
		private Integer rowIndex;
		private Integer colIndex;
		private Integer width;
		private Integer height;
		private Map<String, Object> config;
		private String dataSourceType;
		private UUID dataSourceId;
	}
}