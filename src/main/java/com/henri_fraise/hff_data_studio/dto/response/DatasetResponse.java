package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
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
  private FileType fileFormat;
  private FileProcessingStatus processingStatus;
  private UUID projectId;
  private String projectName;
  private Integer qualityScore;
  private Integer analysisCount;
}