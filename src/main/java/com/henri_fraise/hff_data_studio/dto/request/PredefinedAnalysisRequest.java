package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredefinedAnalysisRequest {

  @javax.validation.constraints.NotBlank(message = "Analysis name is required")
  private String analysisName;

  private String description;

  @NotNull(message = "Category is required")
  private AnalysisCategory category;

  @NotBlank(message = "Reference script is required")
  private String referenceScript;

  private Map<String, Object> requiredParametersJson;
}
