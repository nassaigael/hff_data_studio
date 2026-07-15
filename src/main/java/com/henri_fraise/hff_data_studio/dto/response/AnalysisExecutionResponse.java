package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisExecutionResponse {
  private UUID executionId;
  private LocalDateTime executedAt;
  private AnalysisExecutionStatus status;
  private String statusLabel;
  private Integer durationMs;
  private String durationFormatted;
  private String usedParametersJson;
  private UUID datasetId;
  private String datasetName;
  private UUID analysisId;
  private String analysisName;
  private AnalysisCategory analysisCategory;
  private UUID userId;
  private String userFullName;
  private List<AnalysisResultResponse> results;
  private Integer resultCount;
}
