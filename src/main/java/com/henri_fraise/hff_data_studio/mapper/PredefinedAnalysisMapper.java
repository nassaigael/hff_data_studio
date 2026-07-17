package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.PredefinedAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import org.springframework.stereotype.Component;

@Component
public class PredefinedAnalysisMapper {

  public PredefinedAnalysisResponse toResponse(PredefinedAnalysis analysis) {
    if (analysis == null) {
      return null;
    }

    return PredefinedAnalysisResponse.builder()
            .analysisId(analysis.getId())
            .analysisName(analysis.getAnalysisName())
            .description(analysis.getDescription())
            .category(String.valueOf(analysis.getCategory()))
            .categoryLabel(getCategoryLabel(String.valueOf(analysis.getCategory())))
            .referenceScript(analysis.getReferenceScript())
            .requiredParametersJson(analysis.getRequiredParametersJson())
            .executionCount(
                    analysis.getAnalysisExecutions() != null
                            ? (long) analysis.getAnalysisExecutions().size()
                            : 0L
            )
            .build();
  }

  public PredefinedAnalysis toEntity(PredefinedAnalysisRequest request) {
    if (request == null) {
      return null;
    }

    return PredefinedAnalysis.builder()
            .analysisName(request.getAnalysisName())
            .description(request.getDescription())
            .category(request.getCategory() != null ? request.getCategory() : null)
            .referenceScript(request.getReferenceScript())
            .requiredParametersJson(request.getRequiredParametersJson())
            .build();
  }

  public void updateEntity(PredefinedAnalysis analysis, PredefinedAnalysisRequest request) {
    if (analysis == null || request == null) {
      return;
    }

    if (request.getAnalysisName() != null) {
      analysis.setAnalysisName(request.getAnalysisName());
    }
    if (request.getDescription() != null) {
      analysis.setDescription(request.getDescription());
    }
    if (request.getCategory() != null) {
      analysis.setCategory(request.getCategory());
    }
    if (request.getReferenceScript() != null) {
      analysis.setReferenceScript(request.getReferenceScript());
    }
    if (request.getRequiredParametersJson() != null) {
      analysis.setRequiredParametersJson(request.getRequiredParametersJson());
    }
  }

  private String getCategoryLabel(String category) {
    if (category == null) {
      return null;
    }
    try {
      AnalysisCategory cat = AnalysisCategory.valueOf(category);
      return switch (cat) {
        case STATISTICAL -> "Statistical Analysis";
        case CORRELATION -> "Correlation Analysis";
        case TEMPORAL -> "Temporal Analysis";
        case SEGMENTATION -> "Data Segmentation";
        case FINANCIAL -> "Financial Analysis";
      };
    } catch (IllegalArgumentException e) {
      return category;
    }
  }
}