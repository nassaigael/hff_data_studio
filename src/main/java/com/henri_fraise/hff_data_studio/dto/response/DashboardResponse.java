package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.WidgetType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
	private UUID dashboardId;
	private String name;
	private String description;
	private Boolean isDefault;
	private Boolean isPublic;
	private UUID projectId;
	private UUID userId;
	private List<WidgetResponse> widgets;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WidgetResponse {
		private UUID widgetId;
		private WidgetType type;
		private String title;
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