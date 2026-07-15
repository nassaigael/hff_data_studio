package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import org.springframework.stereotype.Component;

@Component
public class PredefinedAnalysisMapper {

  public PredefinedAnalysisResponse toResponse(PredefinedAnalysis analysis) {
    if (analysis == null) return null;
    return PredefinedAnalysisResponse.builder()
        .analysisId(analysis.getId())
        .analysisName(analysis.getAnalysisName())
        .description(analysis.getDescription())
        .category(analysis.getCategory())
        .categoryLabel(getCategoryLabel(analysis.getCategory()))
        .referenceScript(analysis.getReferenceScript())
        .requiredParametersJson(analysis.getRequiredParametersJson())
        .executionCount(
            analysis.getAnalysisExecutions() != null ? analysis.getAnalysisExecutions().size() : 0L)
        .build();
  }

  private String getCategoryLabel(AnalysisCategory category) {
    if (category == null) return null;
    return switch (category) {
      case STATISTICAL -> "Statistical Analysis";
      case CORRELATION -> "Correlation Analysis";
      case TEMPORAL -> "Temporal Analysis";
      case SEGMENTATION -> "Segmentation Analysis";
      case FINANCIAL -> "Financial Analysis";
      default -> category.name();
    };
  }
  ;
}
