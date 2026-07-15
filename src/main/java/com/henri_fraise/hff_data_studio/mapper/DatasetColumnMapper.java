package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.DatasetColumnResponse;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import java.text.DecimalFormat;
import org.springframework.stereotype.Component;

@Component
public class DatasetColumnMapper {

  private static final DecimalFormat df = new DecimalFormat("0.00");

  public DatasetColumnResponse toResponse(DatasetColumn column) {
    if (column == null) return null;

    int rowCount = column.getDataset() != null ? column.getDataset().getRowCount() : 0;
    double nullPercentage = rowCount > 0 ? (column.getNullCount() * 100.0) / rowCount : 0;
    double uniquePercentage = rowCount > 0 ? (column.getUniqueCount() * 100.0) / rowCount : 0;

    return DatasetColumnResponse.builder()
        .columnId(column.getId())
        .originalName(column.getOriginalName())
        .normalizedName(column.getNormalizedName())
        .detectedType(column.getDetectedType())
        .targetType(column.getTargetType())
        .position(column.getPosition())
        .nullCount(column.getNullCount())
        .nullPercentage(Double.valueOf(df.format(nullPercentage)))
        .uniqueCount(column.getUniqueCount())
        .uniquePercentage(Double.valueOf(df.format(uniquePercentage)))
        .cleaningRuleCount(column.getCleaningRules() != null ? column.getCleaningRules().size() : 0)
        .datasetId(column.getDataset() != null ? column.getDataset().getId() : null)
        .build();
  }

  public void updateEntity(DatasetColumn column, DatasetColumnUpdateRequest request) {
    if (request == null || column == null) return;
    if (request.getNormalizedName() != null) column.setNormalizedName(request.getNormalizedName());
    if (request.getTargetType() != null) column.setTargetType(request.getTargetType());
  }
}
