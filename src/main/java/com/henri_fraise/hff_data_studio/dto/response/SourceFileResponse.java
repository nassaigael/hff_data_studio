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
public class SourceFileResponse {
  private UUID fileId;
  private String fileName;
  private FileType fileType;
  private String storagePath;
  private Long sizesBytes;
  private String sizesFormatted;
  private LocalDateTime uploadAt;
  private FileProcessingStatus processingStatus;
  private UUID projectId;
  private String projectName;
  private UUID userId;
  private String userFullName;
  private Integer datasetCount;
}
