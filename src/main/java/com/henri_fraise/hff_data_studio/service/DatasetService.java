package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.DatasetMapper;
import com.henri_fraise.hff_data_studio.repository.DatasetRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomDatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

	private final DatasetRepository datasetRepository;
	private final CustomDatasetRepository customDatasetRepository;
	private final DatasetMapper datasetMapper;
	private final SourceFileService sourceFileService;
	private final AuditLogService auditLogService;

	public Dataset getDatasetEntityById(UUID datasetId) {
		return datasetRepository.findById(datasetId)
				.orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + datasetId));
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
			sourceFileService.getFileEntityById(fileId);
			Page<Dataset> datasets = datasetRepository.findBySourceFileId(fileId, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
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
		Page<Dataset> datasets = datasetRepository.findAll(pageable);
		return datasets.map(datasetMapper::toResponse);
	}

	public Dataset getDatasetByFileAndName(UUID fileId, String datasetName) {
		return datasetRepository.findBySourceFileIdAndDatasetName(fileId, datasetName)
				.orElseThrow(() -> new ResourceNotFoundException("Dataset not found with name: " + datasetName));
	}

	@Transactional
	public Dataset createDataset(SourceFile sourceFile, String datasetName, Integer rowCount, Integer columnCount) {
		try {
			Dataset dataset = Dataset.builder()
					.datasetName(datasetName)
					.rowCount(rowCount != null ? rowCount : 0)
					.columnCount(columnCount != null ? columnCount : 0)
					.isCleaned(false)
					.sourceFile(sourceFile)
					.build();

			Dataset saved = datasetRepository.save(dataset);
			log.info(" Dataset created successfully: {} ({})", saved.getDatasetName(), saved.getId());

			auditLogService.logAction(
					"DATASET_CREATED",
					"Dataset",
					saved.getId(),
					"Dataset " + saved.getDatasetName() + " created from file: " + sourceFile.getFileName()
			);

			return saved;
		} catch (Exception ex) {
			log.error("Error creating dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create dataset", ex);
		}
	}

	@Transactional
	public void createDataset(Dataset dataset) {
		try {
			Dataset saved = datasetRepository.save(dataset);
			log.info("Dataset created successfully: {} ({})", saved.getDatasetName(), saved.getId());
		} catch (Exception ex) {
			log.error("Error creating dataset: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create dataset", ex);
		}
	}

	@Transactional
	public Dataset createDatasetFromFile(SourceFile sourceFile) {
		try {
			String datasetName = sourceFile.getFileName().replaceAll("\\.[^.]+$", "");
			Dataset dataset = Dataset.builder()
					.datasetName(datasetName)
					.rowCount(0)
					.columnCount(0)
					.isCleaned(false)
					.sourceFile(sourceFile)
					.build();

			Dataset saved = datasetRepository.save(dataset);
			log.info("Dataset created from file: {} ({})", saved.getDatasetName(), saved.getId());
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
			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating dataset name: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update dataset name", ex);
		}
	}

	@Transactional
	public DatasetResponse markAsCleaned(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			dataset.setIsCleaned(true);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset marked as cleaned: {} ({})", updated.getDatasetName(), updated.getId());

			auditLogService.logAction(
					"DATASET_CLEANED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " marked as cleaned"
			);

			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error marking dataset as cleaned: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to mark dataset as cleaned", ex);
		}
	}

	@Transactional
	public void updateDatasetCleanedStatus(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		dataset.setIsCleaned(true);
		datasetRepository.save(dataset);
	}

	@Transactional
	public Dataset updateDatasetCleanedStatus(UUID datasetId, boolean isCleaned) {
		Dataset dataset = getDatasetEntityById(datasetId);
		dataset.setIsCleaned(isCleaned);
		return datasetRepository.save(dataset);
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
			log.info("Dataset deleted successfully : {} ({})", dataset.getDatasetName(), datasetId);

			auditLogService.logAction(
					"DATASET_DELETED",
					"Dataset",
					datasetId,
					"Dataset " + dataset.getDatasetName() + " deleted"
			);
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
					"Dataset " + dataset.getDatasetName() + " deleted by " + userId
			);
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
			List<Dataset> datasets = datasetRepository.findBySourceFileProjectIdOrderByCreatedAtDesc(projectId);
			if (!datasets.isEmpty()) {
				datasetRepository.deleteAll(datasets);
				log.info("Deleted {} datasets for project: {}", datasets.size(), projectId);
			}
		} catch (Exception ex) {
			log.error("Error deleting datasets by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete datasets by project", ex);
		}
	}

	public Page<DatasetResponse> searchProjectDatasets(UUID projectId, String searchTerm, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.searchProjectDatasets(projectId, searchTerm, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching project datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search project datasets", ex);
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

	public List<Dataset> getRecentDatasets(int limit) {
		return datasetRepository.findRecentDatasets(limit);
	}

	public List<Dataset> getRecentDatasetsByProject(UUID projectId, int limit) {
		return datasetRepository.findRecentDatasetsByProjectId(projectId, limit);
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
}