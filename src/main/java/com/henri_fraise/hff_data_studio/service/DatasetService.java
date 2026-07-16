package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
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

	// ==================== CRUD Operations ====================

	public Page<DatasetResponse> getDatasetsByProject(UUID projectId, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.findBySourceFileProjectId(projectId, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving datasets by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve datasets by project", ex);
		}
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

	public DatasetResponse getDatasetById(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		return datasetMapper.toResponse(dataset);
	}

	public Dataset getDatasetEntityById(UUID datasetId) {
		return datasetRepository.findById(datasetId)
				.orElseThrow(() -> new ResourceNotFoundException("Dataset", datasetId));
	}

	public Dataset getDatasetEntityByIdAndProject(UUID datasetId, UUID projectId) {
		Dataset dataset = getDatasetEntityById(datasetId);
		if (!dataset.getSourceFile().getProject().getId().equals(projectId)) {
			throw new ResourceNotFoundException("Dataset not found in project");
		}
		return dataset;
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
	public DatasetResponse updateDatasetStats(UUID datasetId, Integer rowCount, Integer columnCount) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			dataset.setRowCount(rowCount);
			dataset.setColumnCount(columnCount);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset stats updated: {} ({})", updated.getDatasetName(), updated.getId());
			return datasetMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating dataset stats: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update dataset stats", ex);
		}
	}

	@Transactional
	public DatasetResponse markAsCleaned(UUID datasetId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			dataset.setIsCleaned(true);
			Dataset updated = datasetRepository.save(dataset);

			log.info("Dataset marked as cleaned: {} ({})", updated.getDatasetName(), updated.getId());

			// Audit log
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
	public void deleteDataset(UUID datasetId, UUID userId) {
		Dataset dataset = getDatasetEntityById(datasetId);

		try {
			// Delete associated data (columns, exploration report, cleaning history, etc.)
			// Cascade should handle this if configured properly

			datasetRepository.delete(dataset);
			log.info("Dataset deleted successfully: {} ({})", dataset.getDatasetName(), datasetId);

			// Audit log
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

	// ==================== Search Operations ====================

	public Page<DatasetResponse> searchProjectDatasets(UUID projectId, String searchTerm, Pageable pageable) {
		try {
			Page<Dataset> datasets = datasetRepository.searchProjectDatasets(projectId, searchTerm, pageable);
			return datasets.map(datasetMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching project datasets: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search project datasets", ex);
		}
	}

	public List<Dataset> getUncleanedDatasetsOlderThan(int days) {
		LocalDateTime threshold = LocalDateTime.now().minusDays(days);
		return datasetRepository.findUncleanedDatasetsOlderThan(threshold);
	}

	// ==================== Statistics Operations ====================

	public DatasetStatisticsResponse getDatasetStatistics() {
		try {
			return customDatasetRepository.getDatasetStatistics();
		} catch (Exception ex) {
			log.error("Error retrieving dataset statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve dataset statistics", ex);
		}
	}

	public long countDatasetsByProject(UUID projectId) {
		return customDatasetRepository.countDatasetsByProjectId(projectId);
	}

	public long countCleanedDatasetsByProject(UUID projectId) {
		return customDatasetRepository.countCleanedDatasetsByProjectId(projectId);
	}

	public double getAverageDatasetQualityScore() {
		return customDatasetRepository.getAverageDatasetQualityScore();
	}

	// ==================== Validation Methods ====================

	public boolean existsByDatasetNameAndFile(String datasetName, UUID fileId) {
		return datasetRepository.existsByDatasetNameAndSourceFileId(datasetName, fileId);
	}
}