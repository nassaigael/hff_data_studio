package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CleaningHistoryResponse {
    private UUID historyId;
    private LocalDateTime executedAt;
    private CleaningHistoryStatus status;
    private String statusLabel;
    private String details;
    private UUID datasetId;
    private String datasetName;
    private UUID ruleId;
    private RuleType ruleType;
    private UUID userId;
    private String userFullName;
}
