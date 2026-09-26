package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.DashboardRequest;
import com.henri_fraise.hff_data_studio.dto.response.DashboardResponse;
import com.henri_fraise.hff_data_studio.entity.Dashboard;
import com.henri_fraise.hff_data_studio.entity.DashboardWidget;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.DashboardRepository;
import com.henri_fraise.hff_data_studio.repository.DashboardWidgetRepository;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

	private final DashboardRepository dashboardRepository;
	private final DashboardWidgetRepository widgetRepository;
	private final ProjectRepository projectRepository;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;

	@Transactional
	public DashboardResponse create(DashboardRequest request) {
		Dashboard dashboard = Dashboard.builder()
				.name(request.getName())
				.description(request.getDescription())
				.isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
				.isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
				.project(request.getProjectId() != null
						? projectRepository.findById(request.getProjectId()).orElse(null)
						: null)
				.user(securityUtils.getCurrentUser())
				.build();
		Dashboard saved = dashboardRepository.save(dashboard);

		if (request.getWidgets() != null) {
			for (DashboardRequest.WidgetRequest wr : request.getWidgets()) {
				DashboardWidget widget = toWidget(saved, wr);
				widgetRepository.save(widget);
			}
		}

		return getById(saved.getId());
	}

	@Transactional(readOnly = true)
	public DashboardResponse getById(UUID dashboardId) {
		Dashboard dashboard = getEntity(dashboardId);
		return toResponse(dashboard);
	}

	@Transactional(readOnly = true)
	public List<DashboardResponse> list() {
		return dashboardRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public DashboardResponse update(UUID dashboardId, DashboardRequest request) {
		Dashboard dashboard = getEntity(dashboardId);
		dashboard.setName(request.getName());
		dashboard.setDescription(request.getDescription());
		if (request.getIsDefault() != null) dashboard.setIsDefault(request.getIsDefault());
		if (request.getIsPublic() != null) dashboard.setIsPublic(request.getIsPublic());
		dashboardRepository.save(dashboard);

		if (request.getWidgets() != null) {
			widgetRepository.deleteByDashboardId(dashboardId);
			for (DashboardRequest.WidgetRequest wr : request.getWidgets()) {
				widgetRepository.save(toWidget(dashboard, wr));
			}
		}

		return getById(dashboardId);
	}

	@Transactional
	public void delete(UUID dashboardId) {
		dashboardRepository.delete(getEntity(dashboardId));
	}

	private Dashboard getEntity(UUID id) {
		return dashboardRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Dashboard not found"));
	}

	private DashboardWidget toWidget(Dashboard dashboard, DashboardRequest.WidgetRequest wr) {
		return DashboardWidget.builder()
				.dashboard(dashboard)
				.type(wr.getType())
				.title(wr.getTitle())
				.position(wr.getPosition() != null ? wr.getPosition() : 0)
				.rowIndex(wr.getRowIndex() != null ? wr.getRowIndex() : 0)
				.colIndex(wr.getColIndex() != null ? wr.getColIndex() : 0)
				.width(wr.getWidth() != null ? wr.getWidth() : 4)
				.height(wr.getHeight() != null ? wr.getHeight() : 3)
				.configJson(serialize(wr.getConfig()))
				.dataSourceType(wr.getDataSourceType())
				.dataSourceId(wr.getDataSourceId())
				.build();
	}

	private DashboardResponse toResponse(Dashboard dashboard) {
		List<DashboardResponse.WidgetResponse> widgets = dashboard.getWidgets() != null
				? dashboard.getWidgets().stream().map(this::toWidgetResponse).toList()
				: new ArrayList<>();

		return DashboardResponse.builder()
				.dashboardId(dashboard.getId())
				.name(dashboard.getName())
				.description(dashboard.getDescription())
				.isDefault(dashboard.getIsDefault())
				.isPublic(dashboard.getIsPublic())
				.projectId(dashboard.getProject() != null ? dashboard.getProject().getId() : null)
				.userId(dashboard.getUser() != null ? dashboard.getUser().getId() : null)
				.widgets(widgets)
				.createdAt(dashboard.getCreatedAt())
				.updatedAt(dashboard.getUpdatedAt())
				.build();
	}

	private DashboardResponse.WidgetResponse toWidgetResponse(DashboardWidget widget) {
		return DashboardResponse.WidgetResponse.builder()
				.widgetId(widget.getId())
				.type(widget.getType())
				.title(widget.getTitle())
				.position(widget.getPosition())
				.rowIndex(widget.getRowIndex())
				.colIndex(widget.getColIndex())
				.width(widget.getWidth())
				.height(widget.getHeight())
				.config(deserialize(widget.getConfigJson()))
				.dataSourceType(widget.getDataSourceType())
				.dataSourceId(widget.getDataSourceId())
				.build();
	}

	private String serialize(Map<String, Object> config) {
		try {
			return config != null ? objectMapper.writeValueAsString(config) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> deserialize(String json) {
		try {
			return json != null ? objectMapper.readValue(json, Map.class) : Map.of();
		} catch (Exception e) {
			return Map.of();
		}
	}
}