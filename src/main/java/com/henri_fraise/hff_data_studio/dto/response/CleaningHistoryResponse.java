package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CleaningHistoryResponse {
  private UUID historyId;
  private UUID datasetId;
  private UUID ruleId;
  private UUID userId;
  private LocalDateTime executedAt;
  private String executedAtFormatted;
  private CleaningHistoryStatus status;
  private String statusLabel;
  private String details;
  private Integer durationMs;
  private String durationFormatted;
  private Integer affectedRows;
  private String datasetName;
  private Integer datasetRowCount;
  private RuleType ruleType;
  private String ruleTypeLabel;
  private String columnName;
  private String ruleParameters;
  private String userFullName;
  private String userEmail;
}
