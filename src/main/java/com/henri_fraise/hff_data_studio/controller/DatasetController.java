package com.henri_fraise.hff_data_studio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.DatasetColumnUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.*;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import com.henri_fraise.hff_data_studio.service.DataQualityClient;
import com.henri_fraise.hff_data_studio.service.DatasetRowReaderService;
import com.henri_fraise.hff_data_studio.service.DatasetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/datasets")
@RequiredArgsConstructor
public class DatasetController {

	private final DatasetService datasetService;
	private final PageMapper pageMapper;
	private final SecurityUtils securityUtils;
	private final DatasetRowReaderService datasetRowReaderService;
	private final DataQualityClient dataQualityClient;
	private final ObjectMapper objectMapper;

	@GetMapping("/{datasetId}/profiling")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<ColumnProfileResponse>> getProfiling(@PathVariable UUID datasetId) {
		DatasetService.DatasetFileInfo fileInfo = datasetService.getFileInfo(datasetId);
		List<DatasetColumnResponse> columns = datasetService.getDatasetColumns(datasetId);

		Map<String, String> typesMap =
				columns.stream()
						.collect(
								Collectors.toMap(
										DatasetColumnResponse::getOriginalName,
										c -> c.getDetectedType().name()));

		String columnTypesJson;
		try {
			columnTypesJson = objectMapper.writeValueAsString(typesMap);
		} catch (Exception ex) {
			throw new DatabaseException("Cannot serialize column types", ex);
		}

		List<ColumnProfileResponse> profiles =
				dataQualityClient.profileAndSuggest(Paths.get(fileInfo.storagePath()), columnTypesJson);
		return ResponseEntity.ok(profiles);
	}

	@GetMapping("/{datasetId}/rows")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetRowsResponse> getDatasetRows(
			@PathVariable UUID datasetId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "50") @Min(1) @Max(500) int size) {
		DatasetService.DatasetFileInfo info = datasetService.getFileInfo(datasetId);
		return ResponseEntity.ok(
				datasetRowReaderService.readRows(info.storagePath(), info.fileType(), page, size));
	}

	@GetMapping("/{datasetId}")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetResponse> getDatasetById(@PathVariable UUID datasetId) {
		return ResponseEntity.ok(datasetService.getDatasetById(datasetId));
	}

	@GetMapping("/{datasetId}/data")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<Map<String, Object>> getDatasetData(
			@PathVariable UUID datasetId,
			@PageableDefault(size = 50) Pageable pageable,
			@RequestParam(required = false) String sort,
			@RequestParam(required = false) String filter,
			@RequestParam(defaultValue = "CLEANED") String version) {
		return ResponseEntity.ok(
				datasetService.getDatasetData(datasetId, pageable, sort, filter, version));
	}

	@GetMapping("/{datasetId}/columns")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetColumnResponse>> getDatasetColumns(
			@PathVariable UUID datasetId) {
		return ResponseEntity.ok(datasetService.getDatasetColumns(datasetId));
	}

	@GetMapping("/{datasetId}/columns/{columnId}")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetColumnResponse> getColumnById(
			@PathVariable UUID datasetId, @PathVariable UUID columnId) {
		return ResponseEntity.ok(datasetService.getColumnById(datasetId, columnId));
	}

	@PutMapping("/{datasetId}/columns/{columnId}")
	@PreAuthorize("hasAuthority('CLEANING_RULE_UPDATE')")
	public ResponseEntity<DatasetColumnResponse> updateColumn(
			@PathVariable UUID datasetId,
			@PathVariable UUID columnId,
			@Valid @RequestBody DatasetColumnUpdateRequest request) {
		return ResponseEntity.ok(datasetService.updateColumn(datasetId, columnId, request));
	}

	@GetMapping("/project/{projectId}")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> getDatasetsByProject(
			@PathVariable String projectId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets = datasetService.getDatasetsByProject(projectId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@GetMapping("/file/{fileId}")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> getDatasetsByFile(
			@PathVariable UUID fileId, @PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets = datasetService.getDatasetsByFile(fileId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@GetMapping("/search")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> searchDatasets(
			@RequestParam String searchTerm, @PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets = datasetService.searchAllDatasets(searchTerm, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@GetMapping("/project/{projectId}/search")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> searchProjectDatasets(
			@PathVariable String projectId,
			@RequestParam String searchTerm,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets =
				datasetService.searchProjectDatasets(projectId, searchTerm, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@GetMapping("/statistics")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetStatisticsResponse> getDatasetStatistics() {
		return ResponseEntity.ok(datasetService.getDatasetStatistics());
	}

	@GetMapping("/statistics/map")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<Map<String, Object>> getDatasetStatisticsMap() {
		return ResponseEntity.ok(datasetService.getDatasetStatisticsMap());
	}

	@GetMapping("/cleaned")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> getCleanedDatasets(
			@PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets = datasetService.getCleanedDatasets(pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@GetMapping("/uncleaned")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<PageResponse<DatasetResponse>> getUncleanedDatasets(
			@PageableDefault(size = 20) Pageable pageable) {
		Page<DatasetResponse> datasets = datasetService.getUncleanedDatasets(pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(datasets, dataset -> dataset));
	}

	@PatchMapping("/{datasetId}/clean")
	@PreAuthorize("hasAuthority('CLEANING_EXECUTE')")
	public ResponseEntity<DatasetResponse> markAsCleaned(@PathVariable UUID datasetId) {
		return ResponseEntity.ok(datasetService.markAsCleaned(datasetId));
	}

	@PatchMapping("/{datasetId}/unclean")
	@PreAuthorize("hasAuthority('CLEANING_EXECUTE')")
	public ResponseEntity<DatasetResponse> markAsUncleaned(@PathVariable UUID datasetId) {
		return ResponseEntity.ok(datasetService.markAsUncleaned(datasetId));
	}

	@DeleteMapping("/{datasetId}")
	@PreAuthorize("hasAuthority('FILE_DELETE')")
	public ResponseEntity<Void> deleteDataset(@PathVariable UUID datasetId) {
		UUID userId = securityUtils.getCurrentUserId();
		datasetService.deleteDataset(datasetId, userId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{datasetId}/name")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetResponse> updateDatasetName(
			@PathVariable UUID datasetId, @RequestParam String datasetName) {
		return ResponseEntity.ok(datasetService.updateDatasetName(datasetId, datasetName));
	}

	@PatchMapping("/{datasetId}/stats")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetResponse> updateDatasetStats(
			@PathVariable UUID datasetId,
			@RequestParam(required = false) Integer rowCount,
			@RequestParam(required = false) Integer columnCount) {
		return ResponseEntity.ok(datasetService.updateDatasetStats(datasetId, rowCount, columnCount));
	}

	@GetMapping("/recent")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetResponse>> getRecentDatasets(
			@RequestParam(defaultValue = "10") int limit) {
		return ResponseEntity.ok(datasetService.getRecentDatasets(limit));
	}

	@GetMapping("/project/{projectId}/recent")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetResponse>> getRecentDatasetsByProject(
			@PathVariable String projectId,
			@RequestParam(defaultValue = "10") int limit) {
		return ResponseEntity.ok(datasetService.getRecentDatasetsByProject(projectId, limit));
	}


}