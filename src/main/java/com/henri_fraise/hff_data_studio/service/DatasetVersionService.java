package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetVersionResponse;
import com.henri_fraise.hff_data_studio.dto.response.VersionDiffResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetVersion;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.DatasetVersionRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.DatasetService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetVersionService {

	private final DatasetVersionRepository versionRepository;
	private final DatasetService datasetService;
	private final SecurityUtils securityUtils;

	@Transactional
	public DatasetVersionResponse createVersion(UUID datasetId, String label) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		long count = versionRepository.countByDatasetId(datasetId);
		int nextVersion = (int) count + 1;

		versionRepository.findByDatasetIdAndIsCurrentTrue(datasetId).ifPresent(v -> {
			v.setIsCurrent(false);
			versionRepository.save(v);
		});

		DatasetVersion version = DatasetVersion.builder()
				.dataset(dataset)
				.versionNumber(nextVersion)
				.label(label)
				.rowCount(dataset.getRowCount())
				.columnCount(dataset.getColumnCount())
				.isCurrent(true)
				.createdBy(securityUtils.getCurrentUser())
				.build();

		return toResponse(versionRepository.save(version));
	}

	@Transactional(readOnly = true)
	public List<DatasetVersionResponse> listVersions(UUID datasetId) {
		return versionRepository.findByDatasetIdOrderByVersionNumberDesc(datasetId)
				.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public VersionDiffResponse diff(UUID datasetId, Integer fromVersion, Integer toVersion) {
		DatasetVersion from = versionRepository.findByDatasetIdAndVersionNumber(datasetId, fromVersion)
				.orElseThrow(() -> new ResourceNotFoundException("From version not found"));
		DatasetVersion to = versionRepository.findByDatasetIdAndVersionNumber(datasetId, toVersion)
				.orElseThrow(() -> new ResourceNotFoundException("To version not found"));

		return VersionDiffResponse.builder()
				.datasetId(datasetId)
				.fromVersion(fromVersion)
				.toVersion(toVersion)
				.rowCountDelta(to.getRowCount() - from.getRowCount())
				.columnCountDelta(to.getColumnCount() - from.getColumnCount())
				.qualityScoreDelta((to.getQualityScore() != null ? to.getQualityScore() : 0)
						- (from.getQualityScore() != null ? from.getQualityScore() : 0))
				.addedColumns(new ArrayList<>())
				.removedColumns(new ArrayList<>())
				.schemaChanged(!equalsNullable(from.getSchemaHash(), to.getSchemaHash()))
				.dataChanged(!equalsNullable(from.getDataHash(), to.getDataHash()))
				.build();
	}

	private boolean equalsNullable(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}

	private DatasetVersionResponse toResponse(DatasetVersion v) {
		return DatasetVersionResponse.builder()
				.versionId(v.getId())
				.datasetId(v.getDataset() != null ? v.getDataset().getId() : null)
				.versionNumber(v.getVersionNumber())
				.label(v.getLabel())
				.rowCount(v.getRowCount())
				.columnCount(v.getColumnCount())
				.qualityScore(v.getQualityScore())
				.isCurrent(v.getIsCurrent())
				.createdAt(v.getCreatedAt())
				.build();
	}
}