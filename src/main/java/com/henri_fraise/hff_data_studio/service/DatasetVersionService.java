package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetVersionResponse;
import com.henri_fraise.hff_data_studio.dto.response.VersionDiffResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.entity.DatasetVersion;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.DatasetVersionRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetVersionService {

	private final DatasetVersionRepository versionRepository;
	private final DatasetService datasetService;
	private final SecurityUtils securityUtils;

	@Transactional
	public DatasetVersion createInitialVersion(Dataset dataset, String sourceFilePath) {
		long count = versionRepository.countByDatasetId(dataset.getId());
		if (count > 0) {
			return versionRepository
					.findByDatasetIdAndIsCurrentTrue(dataset.getId())
					.orElse(null);
		}

		User creator = null;
		try {
			creator = securityUtils.getCurrentUser();
		} catch (Exception ignored) {
		}

		String dataHash = computeFileHash(sourceFilePath);
		String schemaHash = computeSchemaHash(dataset.getId());
		Double qualityScore = computeQualityScore(dataset.getId(), dataset.getRowCount());

		DatasetVersion version =
				DatasetVersion.builder()
						.dataset(dataset)
						.versionNumber(1)
						.label("Version originale")
						.rowCount(dataset.getRowCount())
						.columnCount(dataset.getColumnCount())
						.snapshotPath(sourceFilePath)
						.dataHash(dataHash)
						.schemaHash(schemaHash)
						.qualityScore(qualityScore)
						.isCurrent(true)
						.createdBy(creator)
						.build();

		DatasetVersion saved = versionRepository.save(version);
		log.info(
				"Initial DatasetVersion created: dataset={} v1 path={} quality={}% dataHash={}",
				dataset.getId(),
				sourceFilePath,
				qualityScore,
				dataHash != null ? dataHash.substring(0, 8) : "null");
		return saved;
	}

	@Transactional
	public DatasetVersionResponse createVersion(UUID datasetId, String label) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		int nextVersion = getNextVersionNumber(datasetId);

		versionRepository
				.findByDatasetIdAndIsCurrentTrue(datasetId)
				.ifPresent(
						v -> {
							v.setIsCurrent(false);
							versionRepository.save(v);
						});

		DatasetVersion version =
				DatasetVersion.builder()
						.dataset(dataset)
						.versionNumber(nextVersion)
						.label(label)
						.rowCount(dataset.getRowCount())
						.columnCount(dataset.getColumnCount())
						.dataHash(computeSchemaHash(datasetId))
						.schemaHash(computeSchemaHash(datasetId))
						.qualityScore(computeQualityScore(datasetId, dataset.getRowCount()))
						.isCurrent(true)
						.createdBy(securityUtils.getCurrentUser())
						.build();

		return toResponse(versionRepository.save(version));
	}

	@Transactional
	public DatasetVersion createVersionWithSnapshot(
			UUID datasetId,
			String label,
			String snapshotPath,
			int rowCount,
			int columnCount) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		int nextVersion = getNextVersionNumber(datasetId);

		versionRepository
				.findByDatasetIdAndIsCurrentTrue(datasetId)
				.ifPresent(
						v -> {
							v.setIsCurrent(false);
							versionRepository.save(v);
						});

		String dataHash = computeFileHash(snapshotPath);
		String schemaHash = computeSchemaHash(datasetId);
		Double qualityScore = computeQualityScore(datasetId, rowCount);

		DatasetVersion version =
				DatasetVersion.builder()
						.dataset(dataset)
						.versionNumber(nextVersion)
						.label(label)
						.rowCount(rowCount)
						.columnCount(columnCount)
						.snapshotPath(snapshotPath)
						.dataHash(dataHash)
						.schemaHash(schemaHash)
						.qualityScore(qualityScore)
						.isCurrent(true)
						.createdBy(securityUtils.getCurrentUser())
						.build();

		DatasetVersion saved = versionRepository.save(version);
		log.info(
				"DatasetVersion created: dataset={} v{} path={} quality={}% dataHash={}",
				datasetId,
				saved.getVersionNumber(),
				snapshotPath,
				qualityScore,
				dataHash != null ? dataHash.substring(0, 8) : "null");
		return saved;
	}

	@Transactional(readOnly = true)
	public int getNextVersionNumber(UUID datasetId) {
		long count = versionRepository.countByDatasetId(datasetId);
		return (int) count + 1;
	}

	@Transactional(readOnly = true)
	public Optional<DatasetVersion> getCurrentVersion(UUID datasetId) {
		return versionRepository.findByDatasetIdAndIsCurrentTrue(datasetId);
	}

	@Transactional(readOnly = true)
	public String getCurrentStoragePath(UUID datasetId, String fallbackPath) {
		return versionRepository
				.findByDatasetIdAndIsCurrentTrue(datasetId)
				.map(DatasetVersion::getSnapshotPath)
				.filter(path -> path != null && !path.isBlank())
				.orElse(fallbackPath);
	}

	@Transactional(readOnly = true)
	public List<DatasetVersionResponse> listVersions(UUID datasetId) {
		return versionRepository.findByDatasetIdOrderByVersionNumberDesc(datasetId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public DatasetVersionResponse restoreVersion(UUID datasetId, UUID versionId) {
		DatasetVersion source =
				versionRepository
						.findById(versionId)
						.orElseThrow(
								() -> new ResourceNotFoundException("Version not found: " + versionId));

		if (!source.getDataset().getId().equals(datasetId)) {
			throw new ResourceNotFoundException("Version does not belong to dataset: " + datasetId);
		}

		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		int nextVersion = getNextVersionNumber(datasetId);

		versionRepository
				.findByDatasetIdAndIsCurrentTrue(datasetId)
				.ifPresent(
						v -> {
							v.setIsCurrent(false);
							versionRepository.save(v);
						});

		String newSnapshotPath = source.getSnapshotPath();
		String dataHash = source.getDataHash();
		if (source.getSnapshotPath() != null && !source.getSnapshotPath().isBlank()) {
			try {
				Path src = Paths.get(source.getSnapshotPath());
				if (Files.exists(src)) {
					String originalPath =
							dataset.getSourceFile() != null ? dataset.getSourceFile().getStoragePath() : null;
					Path dst = buildVersionedPath(originalPath, nextVersion);
					Files.createDirectories(dst.getParent());
					Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
					newSnapshotPath = dst.toString();
					dataHash = computeFileHash(dst.toString());
				}
			} catch (IOException ex) {
				log.error("Failed to copy snapshot for restore: {}", ex.getMessage(), ex);
				throw new DatabaseException("Failed to copy snapshot", ex);
			}
		}

		DatasetVersion restored =
				DatasetVersion.builder()
						.dataset(dataset)
						.versionNumber(nextVersion)
						.label("Restauré depuis v" + source.getVersionNumber())
						.rowCount(source.getRowCount())
						.columnCount(source.getColumnCount())
						.qualityScore(source.getQualityScore())
						.snapshotPath(newSnapshotPath)
						.schemaHash(source.getSchemaHash())
						.dataHash(dataHash)
						.isCurrent(true)
						.createdBy(securityUtils.getCurrentUser())
						.build();

		log.info(
				"DatasetVersion restored: dataset={} from=v{} to=v{}",
				datasetId,
				source.getVersionNumber(),
				nextVersion);
		return toResponse(versionRepository.save(restored));
	}

	@Transactional(readOnly = true)
	public VersionDiffResponse diff(UUID datasetId, Integer fromVersion, Integer toVersion) {
		DatasetVersion from =
				versionRepository
						.findByDatasetIdAndVersionNumber(datasetId, fromVersion)
						.orElseThrow(() -> new ResourceNotFoundException("From version not found"));
		DatasetVersion to =
				versionRepository
						.findByDatasetIdAndVersionNumber(datasetId, toVersion)
						.orElseThrow(() -> new ResourceNotFoundException("To version not found"));

		boolean schemaChanged = false;
		if (from.getSchemaHash() != null && to.getSchemaHash() != null) {
			schemaChanged = !from.getSchemaHash().equals(to.getSchemaHash());
		}

		boolean dataChanged = false;
		if (from.getDataHash() != null && to.getDataHash() != null) {
			dataChanged = !from.getDataHash().equals(to.getDataHash());
		}

		return VersionDiffResponse.builder()
				.datasetId(datasetId)
				.fromVersion(fromVersion)
				.toVersion(toVersion)
				.rowCountDelta(to.getRowCount() - from.getRowCount())
				.columnCountDelta(to.getColumnCount() - from.getColumnCount())
				.qualityScoreDelta(
						(to.getQualityScore() != null ? to.getQualityScore() : 0)
								- (from.getQualityScore() != null ? from.getQualityScore() : 0))
				.addedColumns(new ArrayList<>())
				.removedColumns(new ArrayList<>())
				.schemaChanged(schemaChanged)
				.dataChanged(dataChanged)
				.build();
	}

	public Path buildVersionedPath(String originalPath, int versionNumber) {
		if (originalPath == null || originalPath.isBlank()) {
			throw new DatabaseException("Original path is empty");
		}
		Path original = Paths.get(originalPath);
		String filename = original.getFileName().toString();
		int dotIdx = filename.lastIndexOf('.');
		String name = dotIdx > 0 ? filename.substring(0, dotIdx) : filename;
		String ext = dotIdx > 0 ? filename.substring(dotIdx) : "";
		String versionedName = name + "_v" + versionNumber + ext;
		return original.getParent().resolve(versionedName);
	}

	private String computeFileHash(String filePath) {
		if (filePath == null || filePath.isBlank()) return null;
		try {
			Path path = Paths.get(filePath);
			if (!Files.exists(path)) return null;
			byte[] bytes = Files.readAllBytes(path);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(bytes);
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception ex) {
			log.warn("Cannot compute file hash for {}: {}", filePath, ex.getMessage());
			return null;
		}
	}

	private String computeSchemaHash(UUID datasetId) {
		try {
			List<DatasetColumn> columns = datasetService.getDatasetColumnEntities(datasetId);
			String joined =
					columns.stream()
							.map(c -> c.getOriginalName() + ":" + c.getDetectedType())
							.collect(Collectors.joining(","));
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(joined.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception ex) {
			log.warn("Cannot compute schema hash for {}: {}", datasetId, ex.getMessage());
			return null;
		}
	}

	private Double computeQualityScore(UUID datasetId, int rowCount) {
		try {
			List<DatasetColumn> columns = datasetService.getDatasetColumnEntities(datasetId);
			if (columns.isEmpty() || rowCount == 0) return 100.0;
			long totalCells = (long) rowCount * columns.size();
			long nullCells =
					columns.stream().mapToLong(c -> c.getNullCount() != null ? c.getNullCount() : 0).sum();
			if (totalCells == 0) return 100.0;
			double score = (1.0 - (double) nullCells / totalCells) * 100.0;
			return Math.round(score * 10.0) / 10.0;
		} catch (Exception ex) {
			log.warn("Cannot compute quality score for {}: {}", datasetId, ex.getMessage());
			return null;
		}
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