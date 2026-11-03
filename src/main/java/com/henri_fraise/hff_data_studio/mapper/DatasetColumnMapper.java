package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.DatasetColumnResponse;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import org.springframework.stereotype.Component;

@Component
public class DatasetColumnMapper {

  public DatasetColumnResponse toResponse(DatasetColumn column) {
    if (column == null) return null;

    int rowCount =
            column.getDataset() != null && column.getDataset().getRowCount() != null
                    ? column.getDataset().getRowCount()
                    : 0;

    int nullCount = column.getNullCount() != null ? column.getNullCount() : 0;
    int uniqueCount = column.getUniqueCount() != null ? column.getUniqueCount() : 0;

    double nullPercentage = rowCount > 0 ? (nullCount * 100.0) / rowCount : 0.0;
    double uniquePercentage = rowCount > 0 ? (uniqueCount * 100.0) / rowCount : 0.0;

    return DatasetColumnResponse.builder()
            .columnId(column.getId())
            .originalName(column.getOriginalName())
            .normalizedName(column.getNormalizedName())
            .detectedType(column.getDetectedType())
            .targetType(column.getTargetType())
            .position(column.getPosition())
            .nullCount(nullCount)
            .nullPercentage(roundTwoDecimals(nullPercentage))
            .uniqueCount(uniqueCount)
            .uniquePercentage(roundTwoDecimals(uniquePercentage))
            .cleaningRuleCount(
                    column.getCleaningRules() != null ? column.getCleaningRules().size() : 0)
            .datasetId(column.getDataset() != null ? column.getDataset().getId() : null)
            .build();
  }

  public void updateEntity(DatasetColumn column, DatasetColumnUpdateRequest request) {
    if (request == null || column == null) return;
    if (request.getNormalizedName() != null) column.setNormalizedName(request.getNormalizedName());
    if (request.getTargetType() != null) column.setTargetType(request.getTargetType());
  }

  private double roundTwoDecimals(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}