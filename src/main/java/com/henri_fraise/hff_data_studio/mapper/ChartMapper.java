package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
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

  public String getChartTypeLabel(ChartType type) {
    if (type == null) return null;

    return switch (type) {
      case BAR -> "Bar Chart";
      case LINE -> "Line Chart";
      case PIE -> "Pie Chart";
      case AREA -> "Area Chart";
      case SCATTER -> "Scatter Chart";
    };
  }
}
