package com.henri_fraise.hff_data_studio.dto.request;

import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisExecutionRequest {

  @NotNull(message = "Dataset ID is required")
  private UUID datasetId;

  private UUID analysisId;

  private Map<String, Object> parameters;
}
