package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ColumnType;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetColumnResponse {
  private UUID columnId;
  private String originalName;
  private String normalizedName;
  private ColumnType detectedType;
  private ColumnType targetType;
  private Integer position;
  private Integer nullCount;
  private Double nullPercentage;
  private Integer uniqueCount;
  private Double uniquePercentage;
  private Integer cleaningRuleCount;
  private UUID datasetId;
}
