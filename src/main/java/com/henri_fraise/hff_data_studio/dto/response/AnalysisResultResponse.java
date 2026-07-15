package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ResultType;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResultResponse {
  private UUID resultId;
  private ResultType resultType;
  private String resultTypeLabel;
  private String title;
  private String filePath;
  private String fileFormat;
  private Integer displayOrder;
  private UUID executionId;
  private ChartResponse chart;
  private String downloadUrl;
}
