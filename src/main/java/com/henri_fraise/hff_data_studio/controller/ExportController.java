package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ExportRequest;
import com.henri_fraise.hff_data_studio.dto.response.ExportResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.ExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
public class ExportController {

	private final ExportService exportService;
	private final PageMapper pageMapper;

	@PostMapping
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<ExportResponse> exportResults(
			@Valid @RequestBody ExportRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(exportService.exportResults(request));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<PageResponse<ExportResponse>> getExports(
			@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(required = false) ExportFormat format) {
		Page<ExportResponse> exports = exportService.getExports(pageable, format);
		return ResponseEntity.ok(pageMapper.toPageResponse(exports, e -> e));
	}

	@GetMapping("/{exportId}")
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<ExportResponse> getExportById(@PathVariable UUID exportId) {
		return ResponseEntity.ok(exportService.getExportById(exportId));
	}

	@GetMapping("/{exportId}/download")
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<byte[]> downloadExport(@PathVariable UUID exportId) {
		return exportService.downloadExport(exportId);
	}

	@DeleteMapping("/{exportId}")
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<Void> deleteExport(@PathVariable UUID exportId) {
		exportService.deleteExport(exportId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/execution/{executionId}")
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<PageResponse<ExportResponse>> getExportsByExecution(
			@PathVariable UUID executionId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<ExportResponse> exports = exportService.getExportsByExecution(executionId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(exports, e -> e));
	}

	@GetMapping("/statistics")
	@PreAuthorize("hasAuthority('ANALYSIS_EXPORT')")
	public ResponseEntity<Map<String, Object>> getExportStatistics() {
		return ResponseEntity.ok(exportService.getGlobalStatistics());
	}
}