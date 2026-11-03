package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.DatasetColumnResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.DatasetColumnMapper;
import com.henri_fraise.hff_data_studio.mapper.DatasetMapper;
import com.henri_fraise.hff_data_studio.repository.DatasetColumnRepository;
import com.henri_fraise.hff_data_studio.repository.DatasetRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomDatasetRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

	private static final String ALL_PROJECTS = "all";
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private final DatasetRepository datasetRepository;
	private final DatasetColumnRepository datasetColumnRepository;
	private final CustomDatasetRepository customDatasetRepository;
	private final DatasetMapper datasetMapper;
	private final DatasetColumnMapper datasetColumnMapper;
	private final AuditLogService auditLogService;
	private final DatasetTypeInferenceService typeInferenceService;

	public Dataset getDatasetEntityById(UUID datasetId) {
		return datasetRepository
				.findById(datasetId)
				.orElseThrow(
						() -> new ResourceNotFoundException("Dataset not found with id: " + datasetId));
	}

	public Dataset getDatasetEntityByIdAndProject(UUID datasetId, UUID projectId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		if (!dataset.getSourceFile().getProject().getId().equals(projectId)) {
			throw new ResourceNotFoundException("Dataset not found in project: " + projectId);
		}
		return dataset;
	}

	public DatasetResponse getDatasetById(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		return datasetMapper.toResponse(dataset);
	}

	public Page<DatasetResponse> getDatasetsByProject(String projectId, Pageable pageable) {
		UUID projectUuid = resolveProjectIdOrNull(projectId);
		if (projectUuid == null) {
			return getAllDatasets(pageable);
		}
		try {
			Page<Dataset> datasets = datasetRepository.findBySourceFileProjectId(projectUuid, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving datasets by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve datasets by project", ex);
		}
	}

	public Page<DatasetResponse> getDatasetsByProject(UUID projectId, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.findBySourceFileProjectId(projectId, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving datasets by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve datasets by project", ex);
		}
	}

	public List<Dataset> getDatasetsByProjectEntity(UUID projectId) {
		return datasetRepository.findBySourceFileProjectIdOrderByCreatedAtDesc(projectId);
	}

	public Page<DatasetResponse> getDatasetsByFile(UUID fileId, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.findBySourceFileId(fileId, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving datasets by file: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve datasets by file", ex);
		}
	}

	public List<Dataset> getDatasetsByFileEntity(UUID fileId) {
		return datasetRepository.findBySourceFileIdOrderByCreatedAtAsc(fileId);
	}

	public List<Dataset> getAllDatasets() {
		return datasetRepository.findAll();
	}

	public Page<DatasetResponse> getAllDatasets(Pageable pageable) {
		Page<Dataset> datasets = datasetRepository.findAllWithRelations(pageable);
		return datasets.map(datasetMapper::toResponse);
	}

	public Page<DatasetResponse> getCleanedDatasets(Pageable pageable) {
		Page<Dataset> datasets = datasetRepository.findByIsCleanedTrue(pageable);
		return datasets.map(datasetMapper::toResponse);
	}

	public Page<DatasetResponse> getUncleanedDatasets(Pageable pageable) {
		Page<Dataset> datasets = datasetRepository.findByIsCleanedFalse(pageable);
		return datasets.map(datasetMapper::toResponse);
	}

	public Dataset getDatasetByFileAndName(UUID fileId, String datasetName) {
		return datasetRepository
				.findBySourceFileIdAndDatasetName(fileId, datasetName)
				.orElseThrow(
						() -> new ResourceNotFoundException("Dataset not found with name: " + datasetName));
	}

	public List<DatasetColumnResponse> getDatasetColumns(UUID datasetId) {
		try {
			List<DatasetColumn> columns =
					datasetColumnRepository.findByDatasetIdOrderByPositionAsc(datasetId);
			return columns.stream().map(datasetColumnMapper::toResponse).toList();
		} catch (Exception ex) {
			log.error("Error retrieving dataset columns: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve dataset columns", ex);
		}
	}

	public DatasetColumnResponse getColumnById(UUID datasetId, UUID columnId) {
		try {
			DatasetColumn column =
					datasetColumnRepository
							.findByIdAndDatasetId(columnId, datasetId)
							.orElseThrow(
									() -> new ResourceNotFoundException("Column not found with id: " + columnId));
			return datasetColumnMapper.toResponse(column);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving column: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve column", ex);
		}
	}

	@Transactional
	public DatasetColumnResponse updateColumn(
			UUID datasetId, UUID columnId, DatasetColumnUpdateRequest request) {
		try {
			DatasetColumn column =
					datasetColumnRepository
							.findByIdAndDatasetId(columnId, datasetId)
							.orElseThrow(
									() -> new ResourceNotFoundException("Column not found with id: " + columnId));

			datasetColumnMapper.updateEntity(column, request);
			DatasetColumn updated = datasetColumnRepository.save(column);

			log.info("Column updated: {} ({})", updated.getOriginalName(), updated.getId());

			auditLogService.logAction(
					"COLUMN_UPDATED",
					"DatasetColumn",
					columnId,
					"Column " + updated.getOriginalName() + " updated");

			return datasetColumnMapper.toResponse(updated);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error updating column: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update column", ex);
		}
	}

	@Transactional
	public Dataset createDataset(
			SourceFile sourceFile, String datasetName, Integer rowCount, Integer columnCount) {
		try {
			Dataset dataset =
					Dataset.builder()
							.datasetName(datasetName)
							.rowCount(rowCount != null ? rowCount : 0)
							.columnCount(columnCount != null ? columnCount : 0)
							.isCleaned(false)
							.sourceFile(sourceFile)
							.build();

			Dataset saved = datasetRepository.save(dataset);
			log.info("Dataset created successfully: {} ({})", saved.getDatasetName(), saved.getId());

			auditLogService.logAction(
					"DATASET_CREATED",
					"Dataset",
					saved.getId(),
					"Dataset " + saved.getDatasetName() + " created from file: " + sourceFile.getFileName());

			return saved;
		} catch (Exception ex) {
			log.error("Error creating dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create dataset", ex);
		}
	}

	@Transactional
	public Dataset createDataset(Dataset dataset) {
		try {
			Dataset saved = datasetRepository.save(dataset);
			log.info("Dataset created successfully: {} ({})", saved.getDatasetName(), saved.getId());

			auditLogService.logAction(
					"DATASET_CREATED",
					"Dataset",
					saved.getId(),
					"Dataset " + saved.getDatasetName() + " created");

			return saved;
		} catch (Exception ex) {
			log.error("Error creating dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create dataset", ex);
		}
	}

	@Transactional
	public Dataset createDatasetFromFile(SourceFile sourceFile) {
		try {
			String datasetName = sourceFile.getFileName().replaceAll("\\.[^.]+$", "");
			int[] dims = countRowsAndColumns(sourceFile.getStoragePath(), sourceFile.getFileType());

			Dataset dataset =
					Dataset.builder()
							.datasetName(datasetName)
							.rowCount(dims[0])
							.columnCount(dims[1])
							.isCleaned(false)
							.sourceFile(sourceFile)
							.build();

			Dataset saved = datasetRepository.save(dataset);
			log.info(
					"Dataset created from file: {} ({} rows, {} columns) ({})",
					saved.getDatasetName(),
					dims[0],
					dims[1],
					saved.getId());

			typeInferenceService.inferAndSaveColumns(
					saved, sourceFile.getStoragePath(), sourceFile.getFileType());

			return saved;
		} catch (Exception ex) {
			log.error("Error creating dataset from file: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create dataset from file", ex);
		}
	}

	@Transactional
	public Dataset extractDatasetFromFile(SourceFile file) {
		return createDatasetFromFile(file);
	}

	@Transactional
	public Dataset createDatasetEntity(Dataset dataset) {
		return createDataset(dataset);
	}

	@Transactional
	public DatasetResponse updateDatasetStats(UUID datasetId, Integer rowCount, Integer columnCount) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			if (rowCount != null) {
				dataset.setRowCount(rowCount);
			}
			if (columnCount != null) {
				dataset.setColumnCount(columnCount);
			}
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset stats updated: {} ({})", updated.getDatasetName(), updated.getId());
			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating dataset stats: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update dataset stats", ex);
		}
	}

	@Transactional
	public Dataset updateDatasetStats(UUID datasetId, int rowCount, int columnCount) {
		Dataset dataset = getDatasetEntityById(datasetId);
		dataset.setRowCount(rowCount);
		dataset.setColumnCount(columnCount);
		return datasetRepository.save(dataset);
	}

	@Transactional
	public DatasetResponse updateDatasetName(UUID datasetId, String datasetName) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			dataset.setDatasetName(datasetName);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset name updated: {} ({})", updated.getDatasetName(), updated.getId());

			auditLogService.logAction(
					"DATASET_NAME_UPDATED", "Dataset", datasetId, "Dataset name updated to: " + datasetName);

			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating dataset name: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update dataset name", ex);
		}
	}

	@Transactional
	public DatasetResponse markAsCleaned(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		if (dataset.getIsCleaned()) {
			throw new IllegalStateException("Dataset is already cleaned");
		}

		try {
			dataset.setIsCleaned(true);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset marked as cleaned: {} ({})", updated.getDatasetName(), updated.getId());

			auditLogService.logAction(
					"DATASET_CLEANED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " marked as cleaned");

			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error marking dataset as cleaned: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to mark dataset as cleaned", ex);
		}
	}

	@Transactional
	public DatasetResponse markAsUncleaned(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		if (!dataset.getIsCleaned()) {
			throw new IllegalStateException("Dataset is already uncleaned");
		}

		try {
			dataset.setIsCleaned(false);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset marked as uncleaned: {} ({})", updated.getDatasetName(), updated.getId());

			auditLogService.logAction(
					"DATASET_UNCLEANED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " marked as uncleaned");

			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error marking dataset as uncleaned: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to mark dataset as uncleaned", ex);
		}
	}

	@Transactional
	public void updateDatasetCleanedStatus(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		dataset.setIsCleaned(true);
		datasetRepository.save(dataset);
	}

	@Transactional
	public void updateDatasetCleanedStatus(UUID datasetId, boolean isCleaned) {
		Dataset dataset = getDatasetEntityById(datasetId);
		dataset.setIsCleaned(isCleaned);
		datasetRepository.save(dataset);
	}

	@Transactional
	public Dataset updateDataset(Dataset dataset) {
		return datasetRepository.save(dataset);
	}

	@Transactional
	public void deleteDataset(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			datasetRepository.delete(dataset);
			log.info("Dataset deleted successfully: {} ({})", dataset.getDatasetName(), datasetId);

			auditLogService.logAction(
					"DATASET_DELETED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " deleted");
		} catch (Exception ex) {
			log.error("Error deleting dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete dataset", ex);
		}
	}

	@Transactional
	public void deleteDataset(UUID datasetId, UUID userId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			datasetRepository.delete(dataset);
			log.info("Dataset deleted successfully: {} ({})", dataset.getDatasetName(), datasetId);

			auditLogService.logAction(
					"DATASET_DELETED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " deleted by " + userId);
		} catch (Exception ex) {
			log.error("Error deleting dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete dataset", ex);
		}
	}

	@Transactional
	public void deleteDatasetsByFile(UUID fileId) {
		try {
			List<Dataset> datasets = datasetRepository.findBySourceFileIdOrderByCreatedAtAsc(fileId);
			if (!datasets.isEmpty()) {
				datasetRepository.deleteAll(datasets);
				log.info("Deleted {} datasets for file: {}", datasets.size(), fileId);
			}
		} catch (Exception ex) {
			log.error("Error deleting datasets by file: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete datasets by file", ex);
		}
	}

	@Transactional
	public void deleteDatasetsByProject(UUID projectId) {
		try {
			List<Dataset> datasets =
					datasetRepository.findBySourceFileProjectIdOrderByCreatedAtDesc(projectId);
			if (!datasets.isEmpty()) {
				datasetRepository.deleteAll(datasets);
				log.info("Deleted {} datasets for project: {}", datasets.size(), projectId);
			}
		} catch (Exception ex) {
			log.error("Error deleting datasets by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete datasets by project", ex);
		}
	}

	public Page<DatasetResponse> searchProjectDatasets(
			String projectId, String searchTerm, Pageable pageable) {
		UUID projectUuid = resolveProjectIdOrNull(projectId);
		if (projectUuid == null) {
			return searchAllDatasets(searchTerm, pageable);
		}
		try {
			Page<Dataset> datasets =
					datasetRepository.searchProjectDatasets(projectUuid, searchTerm, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching project datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search project datasets", ex);
		}
	}

	public Page<DatasetResponse> searchProjectDatasets(
			UUID projectId, String searchTerm, Pageable pageable) {
		try {
			Page<Dataset> datasets =
					datasetRepository.searchProjectDatasets(projectId, searchTerm, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching project datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search project datasets", ex);
		}
	}

	public Page<DatasetResponse> searchAllDatasets(String searchTerm, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.searchAllDatasets(searchTerm, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching all datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search all datasets", ex);
		}
	}

	public List<Dataset> searchDatasets(String searchTerm) {
		return datasetRepository.findByDatasetNameContainingIgnoreCase(searchTerm);
	}

	public Page<Dataset> searchDatasets(String searchTerm, Pageable pageable) {
		return datasetRepository.findByDatasetNameContainingIgnoreCase(searchTerm, pageable);
	}

	public long countDatasets() {
		return datasetRepository.count();
	}

	public long countDatasetsByProject(UUID projectId) {
		return datasetRepository.countBySourceFileProjectId(projectId);
	}

	public long countDatasetsByFile(UUID fileId) {
		return datasetRepository.countBySourceFileId(fileId);
	}

	public long countCleanedDatasets() {
		return datasetRepository.countByIsCleanedTrue();
	}

	public long countUncleanedDatasets() {
		return datasetRepository.countByIsCleanedFalse();
	}

	public long countCleanedDatasetsByProject(UUID projectId) {
		return datasetRepository.countBySourceFileProjectIdAndIsCleanedTrue(projectId);
	}

	public long countUncleanedDatasetsByProject(UUID projectId) {
		return datasetRepository.countBySourceFileProjectIdAndIsCleanedFalse(projectId);
	}

	public long countDatasetsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return datasetRepository.countByCreatedAtBetween(startDate, endDate);
	}

	public DatasetStatisticsResponse getDatasetStatistics() {
		try {
			return customDatasetRepository.getDatasetStatistics();
		} catch (Exception ex) {
			log.error("Error retrieving dataset statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve dataset statistics", ex);
		}
	}

	public Map<String, Object> getDatasetStatisticsMap() {
		try {
			return customDatasetRepository.getDatasetStatisticsMap();
		} catch (Exception ex) {
			log.error("Error retrieving dataset statistics map: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve dataset statistics map", ex);
		}
	}

	public double getAverageDatasetQualityScore() {
		try {
			return customDatasetRepository.getAverageDatasetQualityScore();
		} catch (Exception ex) {
			log.error("Error getting average dataset quality score: {}", ex.getMessage(), ex);
			return 0.0;
		}
	}

	public double getAverageDatasetQualityScoreByProject(UUID projectId) {
		try {
			return customDatasetRepository.getAverageDatasetQualityScoreByProjectId(projectId);
		} catch (Exception ex) {
			log.error("Error getting average dataset quality score by project: {}", ex.getMessage(), ex);
			return 0.0;
		}
	}

	public long getTotalRows() {
		Long total = datasetRepository.sumRowCount();
		return total != null ? total : 0L;
	}

	public long getTotalRowsByProject(UUID projectId) {
		Long total = datasetRepository.sumRowCountByProjectId(projectId);
		return total != null ? total : 0L;
	}

	public long getTotalColumns() {
		Long total = datasetRepository.sumColumnCount();
		return total != null ? total : 0L;
	}

	public long getTotalColumnsByProject(UUID projectId) {
		Long total = datasetRepository.sumColumnCountByProjectId(projectId);
		return total != null ? total : 0L;
	}

	public double getAverageRowsPerDataset() {
		Double avg = datasetRepository.averageRowCount();
		return avg != null ? avg : 0.0;
	}

	public double getAverageColumnsPerDataset() {
		Double avg = datasetRepository.averageColumnCount();
		return avg != null ? avg : 0.0;
	}

	public boolean existsByDatasetNameAndFile(String datasetName, UUID fileId) {
		return datasetRepository.existsByDatasetNameAndSourceFileId(datasetName, fileId);
	}

	public boolean existsById(UUID datasetId) {
		return datasetRepository.existsById(datasetId);
	}

	public boolean existsByProjectId(UUID projectId) {
		return datasetRepository.existsBySourceFileProjectId(projectId);
	}

	public List<Dataset> getUncleanedDatasetsOlderThan(int days) {
		LocalDateTime threshold = LocalDateTime.now().minusDays(days);
		return datasetRepository.findUncleanedDatasetsOlderThan(threshold);
	}

	public List<Dataset> getDatasetsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return datasetRepository.findByCreatedAtBetween(startDate, endDate);
	}

	public List<DatasetResponse> getRecentDatasets(int limit) {
		List<Dataset> datasets = datasetRepository.findRecentDatasets(Pageable.ofSize(limit));
		return datasets.stream().map(datasetMapper::toResponse).toList();
	}

	public List<DatasetResponse> getRecentDatasetsByProject(String projectId, int limit) {
		UUID projectUuid = resolveProjectIdOrNull(projectId);
		if (projectUuid == null) {
			return getRecentDatasets(limit);
		}
		List<Dataset> datasets =
				datasetRepository.findRecentDatasetsByProjectId(projectUuid, Pageable.ofSize(limit));
		return datasets.stream().map(datasetMapper::toResponse).toList();
	}

	public List<DatasetResponse> getRecentDatasetsByProject(UUID projectId, int limit) {
		List<Dataset> datasets =
				datasetRepository.findRecentDatasetsByProjectId(projectId, Pageable.ofSize(limit));
		return datasets.stream().map(datasetMapper::toResponse).toList();
	}

	public String getDatasetFilePath(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		return dataset.getSourceFile().getStoragePath();
	}

	public SourceFile getSourceFileByDatasetId(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		return dataset.getSourceFile();
	}

	public UUID getProjectIdByDatasetId(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		return dataset.getSourceFile().getProject().getId();
	}

	public Map<String, Object> getDatasetData(
			UUID datasetId, Pageable pageable, String sort, String filter, String version) {
		Map<String, Object> result = new HashMap<>();
		Dataset dataset = getDatasetEntityById(datasetId);
		result.put("datasetId", datasetId);
		result.put("datasetName", dataset.getDatasetName());
		result.put("rowCount", dataset.getRowCount());
		result.put("columnCount", dataset.getColumnCount());
		result.put("isCleaned", dataset.getIsCleaned());
		result.put("version", version);
		return result;
	}

	@Transactional
	public List<Dataset> saveAll(List<Dataset> datasets) {
		try {
			List<Dataset> saved = datasetRepository.saveAll(datasets);
			log.info("Saved {} datasets", saved.size());
			return saved;
		} catch (Exception ex) {
			log.error("Error saving datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to save datasets", ex);
		}
	}

	@Transactional
	public void deleteAll(List<Dataset> datasets) {
		try {
			datasetRepository.deleteAll(datasets);
			log.info("Deleted {} datasets", datasets.size());
		} catch (Exception ex) {
			log.error("Error deleting datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete datasets", ex);
		}
	}

	@Transactional
	public void cleanupOldDatasets(LocalDateTime thresholdDate) {
		try {
			List<Dataset> oldDatasets = datasetRepository.findByCreatedAtBefore(thresholdDate);
			if (!oldDatasets.isEmpty()) {
				datasetRepository.deleteAll(oldDatasets);
				log.info("Cleaned up {} old datasets", oldDatasets.size());
			}
		} catch (Exception ex) {
			log.error("Error cleaning up old datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to cleanup old datasets", ex);
		}
	}

	private UUID resolveProjectIdOrNull(String projectId) {
		if (projectId == null || projectId.isBlank() || ALL_PROJECTS.equalsIgnoreCase(projectId)) {
			return null;
		}
		try {
			return UUID.fromString(projectId);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Invalid projectId: " + projectId, ex);
		}
	}

	private int[] countRowsAndColumns(String storagePath, FileType fileType) {
		if (storagePath == null || fileType == null) {
			return new int[] {0, 0};
		}

		if (fileType == FileType.EXCEL || fileType == FileType.XLSX) {
			return countExcelRowsAndColumns(storagePath);
		}

		if (fileType == FileType.JSON) {
			return countJsonDimensions(storagePath);
		}

		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				log.warn("Storage file not found: {}", storagePath);
				return new int[] {0, 0};
			}

			List<String> lines =
					Files.readAllLines(path, StandardCharsets.UTF_8).stream()
							.filter(line -> !line.isBlank())
							.toList();

			if (lines.isEmpty()) {
				return new int[] {0, 0};
			}

			return switch (fileType) {
				case CSV -> {
					int headerCols = countCsvColumns(lines.get(0));
					int dataRows = Math.max(0, lines.size() - 1);
					yield new int[] {dataRows, headerCols};
				}
				case SQL -> {
					long inserts =
							lines.stream()
									.filter(line -> line.trim().toUpperCase().startsWith("INSERT"))
									.count();
					int columns = countSqlColumns(lines);
					yield new int[] {(int) inserts, columns};
				}
				default -> {
					yield new int[] {lines.size(), 1};
				}
			};
		} catch (IOException ex) {
			log.warn("Cannot read file for row/column count: {}", storagePath, ex);
			return new int[] {0, 0};
		}
	}

	private int countCsvColumns(String headerLine) {
		if (headerLine == null || headerLine.isBlank()) {
			return 0;
		}
		boolean inQuotes = false;
		int columns = 1;
		for (int i = 0; i < headerLine.length(); i++) {
			char c = headerLine.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				columns++;
			}
		}
		return columns;
	}

	private int countSqlColumns(List<String> lines) {
		boolean insideCreate = false;
		int columns = 0;
		for (String raw : lines) {
			String line = raw.trim();
			String upper = line.toUpperCase();
			if (upper.startsWith("CREATE TABLE")) {
				insideCreate = true;
				int open = line.indexOf('(');
				if (open >= 0) {
					String after = line.substring(open + 1).trim();
					if (!after.isEmpty() && !after.startsWith(")")) {
						columns++;
					}
				}
				continue;
			}
			if (insideCreate) {
				if (line.startsWith(")")) {
					break;
				}
				if (!upper.startsWith("PRIMARY")
						&& !upper.startsWith("FOREIGN")
						&& !upper.startsWith("CONSTRAINT")
						&& !upper.startsWith("UNIQUE")
						&& !upper.startsWith("CHECK")
						&& !line.isEmpty()) {
					columns++;
				}
			}
		}
		return Math.max(columns, 1);
	}

	private int[] countJsonDimensions(String storagePath) {
		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				log.warn("Storage file not found: {}", storagePath);
				return new int[] {0, 0};
			}

			String content = Files.readString(path, StandardCharsets.UTF_8).trim();
			if (content.isEmpty()) {
				return new int[] {0, 0};
			}

			JsonNode root = JSON_MAPPER.readTree(content);

			if (root.isArray()) {
				if (root.isEmpty()) {
					return new int[] {0, 0};
				}

				int rows = root.size();
				Set<String> keys = new LinkedHashSet<>();

				for (JsonNode node : root) {
					if (node.isObject()) {
						node.fieldNames().forEachRemaining(keys::add);
					}
				}

				return new int[] {rows, keys.size()};
			}

			if (root.isObject()) {
				return new int[] {1, root.size()};
			}

			return new int[] {0, 0};
		} catch (Exception ex) {
			log.warn("Cannot parse JSON for row/column count: {}", storagePath, ex);
			return new int[] {0, 0};
		}
	}

	private int[] countExcelRowsAndColumns(String storagePath) {
		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				log.warn("Storage file not found: {}", storagePath);
				return new int[] {0, 0};
			}

			try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
				int sheetCount = workbook.getNumberOfSheets();
				if (sheetCount == 0) {
					log.warn("Excel file has no sheets: {}", storagePath);
					return new int[] {0, 0};
				}

				int bestRows = 0;
				int bestCols = 0;
				String bestSheetName = null;

				for (int s = 0; s < sheetCount; s++) {
					Sheet sheet = workbook.getSheetAt(s);
					String sheetName = sheet.getSheetName();
					int lastRow = sheet.getLastRowNum();

					if (lastRow < 1) {
						continue;
					}

					int cols = countExcelColumns(sheet, lastRow);
					int rows = countExcelDataRows(sheet, lastRow);

					if (rows > bestRows) {
						bestRows = rows;
						bestCols = cols;
						bestSheetName = sheetName;
					}
				}

				if (bestSheetName == null) {
					log.warn("No sheet with data found in Excel file: {}", storagePath);
					return new int[] {0, 0};
				}

				log.info(
						"Excel file '{}': using sheet '{}' ({} rows, {} columns)",
						storagePath,
						bestSheetName,
						bestRows,
						bestCols);

				return new int[] {bestRows, bestCols};
			}
		} catch (Exception ex) {
			log.warn("Cannot read Excel file for row/column count: {}", storagePath, ex);
			return new int[] {0, 0};
		}
	}

	private int countExcelColumns(Sheet sheet, int lastRow) {
		int maxCols = 0;

		Row header = sheet.getRow(0);
		if (header != null) {
			for (int i = 0; i < header.getLastCellNum(); i++) {
				Cell cell = header.getCell(i);
				if (cell != null && !getCellValueAsString(cell).isBlank()) {
					maxCols = Math.max(maxCols, i + 1);
				}
			}
		}

		for (int r = 1; r <= lastRow; r++) {
			Row row = sheet.getRow(r);
			if (row == null || isExcelRowEmpty(row)) {
				continue;
			}
			int lastCell = row.getLastCellNum();
			if (lastCell > 0) {
				maxCols = Math.max(maxCols, lastCell);
			}
		}

		return maxCols;
	}

	private int countExcelDataRows(Sheet sheet, int lastRow) {
		int dataRows = 0;
		for (int i = 1; i <= lastRow; i++) {
			Row row = sheet.getRow(i);
			if (row != null && !isExcelRowEmpty(row)) {
				dataRows++;
			}
		}
		return dataRows;
	}

	private boolean isExcelRowEmpty(Row row) {
		int nonEmptyCells = 0;
		String firstText = null;

		for (int c = 0; c < row.getLastCellNum(); c++) {
			Cell cell = row.getCell(c);
			if (cell == null) {
				continue;
			}
			String value = getCellValueAsString(cell);
			if (value.isBlank()) {
				continue;
			}
			nonEmptyCells++;
			if (firstText == null) {
				firstText = value;
			}
		}

		if (nonEmptyCells == 0) {
			return true;
		}

		if (firstText != null) {
			String lower = firstText.toLowerCase().trim();
			if (lower.equals("total") || lower.equals("totaux")) {
				return true;
			}
			if (nonEmptyCells == 1
					&& (lower.startsWith("applied filters")
					|| lower.startsWith("filtres appliqués")
					|| lower.startsWith("filtres appliques"))) {
				return true;
			}
		}

		return false;
	}

	private String getCellValueAsString(Cell cell) {
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue();
			case NUMERIC -> String.valueOf(cell.getNumericCellValue());
			case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
			case FORMULA -> cell.getCellFormula();
			default -> "";
		};
	}
}