package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.AnalysisResultResponse;
import com.henri_fraise.hff_data_studio.dto.response.ChartResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisResultMapper {

  private final ChartMapper chartMapper;

  public AnalysisResultResponse toResponse(AnalysisResult result) {

    if (result == null) return null;

    ChartResponse chartResponse = null;
    if (result.getChart() != null) chartResponse = chartMapper.toResponse(result.getChart());

    String DOWNLOAD_URL = "/api/v1/results/";
    return AnalysisResultResponse.builder()
        .resultId(result.getId())
        .resultType(result.getResultType())
        .resultTypeLabel(getResultTypeLabel(result.getResultType()))
        .title(result.getTitle())
        .filePath(result.getFilePath())
        .fileFormat(String.valueOf(result.getFileFormat()))
        .displayOrder(result.getDisplayOrder())
        .executionId(result.getExecution() != null ? result.getExecution().getId() : null)
        .chart(chartResponse)
        .downloadUrl(DOWNLOAD_URL + result.getId() + "download")
        .build();
  }

  private String getResultTypeLabel(ResultType type) {
    if (type == null) return null;

    return switch (type) {
      case KPI -> "KPI";
      case CHART -> "Chart";
      case TABLE -> "Table";
    };
  }
}
