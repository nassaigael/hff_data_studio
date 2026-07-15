package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import org.springframework.stereotype.Component;

@Component
public class CleaningHistoryMapper {

  public CleaningHistoryResponse toResponse(CleaningHistory history) {
    if (history == null) return null;
    return CleaningHistoryResponse.builder()
        .historyId(history.getId())
        .executedAt(history.getExecutedAt())
        .status(history.getStatus())
        .statusLabel(getStatusLabel(history.getStatus()))
        .details(history.getDetails())
        .datasetId(history.getDataset() != null ? history.getDataset().getId() : null)
        .datasetName(history.getDataset() != null ? history.getDataset().getDatasetName() : null)
        .ruleId(history.getRule() != null ? history.getRule().getId() : null)
        .ruleType(history.getRule() != null ? history.getRule().getRuleType() : null)
        .userId(history.getUser() != null ? history.getUser().getId() : null)
        .userFullName(
            history.getUser() != null
                ? history.getUser().getFirstName() + " " + history.getUser().getLastName()
                : null)
        .build();
  }

  private String getStatusLabel(CleaningHistoryStatus status) {
    if (status == null) return null;
    return switch (status) {
      case SUCCESS -> "Success";
      case FAILURE -> "Failure";
      case IN_PROGRESS -> "In progress";
      case PARTIAL_SUCCESS -> "Partial success";
      case CANCELLED -> "Cancelled";
      case PENDING -> "Pending";
    };
  }
}
