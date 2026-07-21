package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.stereotype.Component;

@Component
public class ChartMapper {

  public ChartResponse toResponse(Chart chart) {
    if (chart == null) {
      return null;
    }

    return ChartResponse.builder()
        .chartId(chart.getId())
        .chartType(chart.getChartType())
        .chartTypeLabel(getChartTypeLabel(chart.getChartType()))
        .configJson(chart.getConfigJson())
        .resultId(chart.getResult() != null ? chart.getResult().getId() : null)
        .resultTitle(chart.getResult() != null ? chart.getResult().getTitle() : null)
        .resultType(chart.getResult() != null ? chart.getResult().getResultType().name() : null)
        .imageUrl(
            chart.getResult() != null
                ? "/api/v1/results/" + chart.getResult().getId() + "/download"
                : null)
        .build();
  }

  private String getChartTypeLabel(ChartType chartType) {
    if (chartType == null) {
      return null;
    }
    return switch (chartType) {
      case BAR -> "Bar Chart";
      case LINE -> "Line Chart";
      case PIE -> "Pie Chart";
      case SCATTER -> "Scatter Plot";
      case AREA -> "Area Chart";
    };
  }
}
