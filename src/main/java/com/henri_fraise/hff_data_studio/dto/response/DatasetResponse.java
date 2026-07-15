package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetResponse {
  private UUID datasetId;
  private String datasetName;
  private Integer rowCount;
  private Integer columnCount;
  private LocalDateTime createdAt;
  private Boolean isCleaned;
  private UUID fileId;
  private String fileName;
  private UUID projectId;
  private String projectName;
  private Integer qualityScore;
  private Integer analysisCount;
}
