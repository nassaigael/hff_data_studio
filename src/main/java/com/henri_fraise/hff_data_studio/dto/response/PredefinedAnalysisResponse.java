package com.henri_fraise.hff_data_studio.dto.response;

import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedAnalysisResponse {

  private UUID analysisId;

  private String analysisName;

  private String description;

  private String category;

  private String categoryLabel;

  private String referenceScript;

  private Map<String, Object> requiredParametersJson;

  private Long executionCount;
}
