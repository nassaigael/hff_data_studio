package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.DatasetColumnResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.DatasetColumnMapper;
import com.henri_fraise.hff_data_studio.repository.DatasetColumnRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetColumnService {

  private final DatasetColumnRepository columnRepository;
  private final DatasetColumnMapper columnMapper;

  public DatasetColumn getColumnEntityById(UUID columnId) {
    return columnRepository
        .findById(columnId)
        .orElseThrow(() -> new ResourceNotFoundException("Dataset column not found: " + columnId));
  }

  public DatasetColumnResponse getColumnById(UUID columnId) {
    DatasetColumn column = getColumnEntityById(columnId);
    return columnMapper.toResponse(column);
  }

  public List<DatasetColumn> getColumnsByDataset(UUID datasetId) {
    return columnRepository.findByDatasetIdOrderByPositionAsc(datasetId);
  }

  public List<DatasetColumnResponse> getColumnResponsesByDataset(UUID datasetId) {
    List<DatasetColumn> columns = getColumnsByDataset(datasetId);
    return columns.stream().map(columnMapper::toResponse).toList();
  }

  @Transactional
  public DatasetColumn createColumn(
      Dataset dataset,
      String originalName,
      ColumnType detectedType,
      int position,
      int nullCount,
      int uniqueCount) {
    DatasetColumn column =
        DatasetColumn.builder()
            .dataset(dataset)
            .originalName(originalName)
            .detectedType(detectedType)
            .position(position)
            .nullCount(nullCount)
            .uniqueCount(uniqueCount)
            .build();
    DatasetColumn saved = columnRepository.save(column);
    log.info(
        "Column created: {} for dataset: {}", saved.getOriginalName(), dataset.getDatasetName());
    return saved;
  }

  @Transactional
  public DatasetColumn updateColumn(UUID columnId, DatasetColumnUpdateRequest request) {
    DatasetColumn column = getColumnEntityById(columnId);
    columnMapper.updateEntity(column, request);
    DatasetColumn updated = columnRepository.save(column);
    log.info("Column updated: {}", columnId);
    return updated;
  }

  @Transactional
  public DatasetColumn updateColumn(UUID columnId, DatasetColumn column) {
    DatasetColumn existing = getColumnEntityById(columnId);
    existing.setNormalizedName(column.getNormalizedName());
    existing.setTargetType(column.getTargetType());
    return columnRepository.save(existing);
  }

  @Transactional
  public void deleteColumn(UUID columnId) {
    DatasetColumn column = getColumnEntityById(columnId);
    columnRepository.delete(column);
    log.info("Column deleted: {}", columnId);
  }

  @Transactional
  public void deleteColumnsByDataset(UUID datasetId) {
    List<DatasetColumn> columns = getColumnsByDataset(datasetId);
    columnRepository.deleteAll(columns);
    log.info("Deleted {} columns for dataset: {}", columns.size(), datasetId);
  }

  public long countColumnsByDataset(UUID datasetId) {
    return columnRepository.countByDatasetId(datasetId);
  }

  public long countNullValuesByDataset(UUID datasetId) {
    Long count = columnRepository.sumNullCountByDatasetId(datasetId);
    return count != null ? count : 0;
  }

  public long countUniqueValuesByDataset(UUID datasetId) {
    Long count = columnRepository.sumUniqueCountByDatasetId(datasetId);
    return count != null ? count : 0;
  }
}
