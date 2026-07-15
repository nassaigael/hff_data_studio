package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningHistory;
import com.henri_fraise.hff_data_studio.enums.CleaningHistoryStatus;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

@Component
public class CleaningHistoryMapper {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  public CleaningHistoryResponse toResponse(CleaningHistory history) {
    if (history == null) return null;

    return CleaningHistoryResponse.builder()
            .historyId(history.getId())
            .executedAt(history.getExecutedAt())
            .executedAtFormatted(history.getExecutedAt() != null ? history.getExecutedAt().format(DATE_FORMATTER) : null)
            .status(history.getStatus())
            .statusLabel(getStatusLabel(history.getStatus()))
            .details(history.getDetails())
            .durationMs(history.getDurationMs())
            .durationFormatted(formatDuration(history.getDurationMs()))
            .affectedRows(history.getAffectedRows())
            .datasetId(history.getDataset() != null ? history.getDataset().getId() : null)
            .datasetName(history.getDataset() != null ? history.getDataset().getDatasetName() : null)
            .datasetRowCount(history.getDataset() != null ? history.getDataset().getRowCount() : null)
            .ruleId(history.getRule() != null ? history.getRule().getId() : null)
            .ruleType(history.getRule() != null ? history.getRule().getRuleType() : null)
            .ruleTypeLabel(history.getRule() != null ? getRuleTypeLabel(history.getRule().getRuleType()) : null)
            .columnName(history.getRule() != null && history.getRule().getColumn() != null
                    ? history.getRule().getColumn().getOriginalName()
                    : null)
            .ruleParameters(history.getRule() != null && history.getRule().getParametersJson() != null
                    ? history.getRule().getParametersJson().toString()
                    : null)
            .userId(history.getUser() != null ? history.getUser().getId() : null)
            .userFullName(history.getUser() != null
                    ? history.getUser().getFirstName() + " " + history.getUser().getLastName()
                    : null)
            .userEmail(history.getUser() != null ? history.getUser().getEmail() : null)
            .build();
  }

  private String getStatusLabel(CleaningHistoryStatus status) {
    if (status == null) return null;
    return switch (status) {
      case SUCCESS -> "Success";
      case FAILURE -> "Failure";
      case PARTIAL_SUCCESS -> "Partial Success";
      case IN_PROGRESS -> "In Progress";
      case CANCELLED -> "Cancelled";
      case PENDING -> "Pending";
    };
  }


  private String getRuleTypeLabel(RuleType ruleType) {
    if (ruleType == null) return null;
    return switch (ruleType) {
      case TYPE_CONVERSION -> "Type Conversion";
      case DUPLICATE_REMOVAL -> "Duplicate Removal";
      case NULL_IMPUTATION -> "Null Imputation";
      case REGEX_CLEANING -> "Regex Cleaning";
      case TRIM -> "Trim Whitespace";
      case VALUE_CONSTRAINT -> "Value Constraint";
    };
  }

  private String formatDuration(Integer durationMs) {
    if (durationMs == null) return null;
    if (durationMs < 1000) return durationMs + " ms";
    if (durationMs < 60000) return String.format("%.2f s", durationMs / 1000.0);
    long minutes = durationMs / 60000;
    long seconds = (durationMs % 60000) / 1000;
    return String.format("%d min %d s", minutes, seconds);
  }
}