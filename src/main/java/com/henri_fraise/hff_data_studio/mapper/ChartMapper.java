package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.Chart;
import org.springframework.stereotype.Component;

@Component
public class ChartMapper {

	public ChartResponse toResponse(Chart chart) {
		if (chart == null) return null;

		return ChartResponse.builder()
				.chartId(chart.getId())
				.chartType(chart.getChartType())
				.chartTypeLabel(getChartTypeLabel(chart.getChartType()))
				.configJson(chart.getConfigJson())
				.resultId(chart.getResult() != null ? chart.getResult().getId() : null)
				.build();
	}
}
