package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.DatasetAnalysisHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetExportHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetUsageSummaryResponse;
import com.henri_fraise.hff_data_studio.service.DatasetUsageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/datasets/{datasetId}/usage")
@RequiredArgsConstructor
public class DatasetUsageController {

	private final DatasetUsageService usageService;

	@GetMapping("/summary")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetUsageSummaryResponse> getSummary(@PathVariable UUID datasetId) {
		return ResponseEntity.ok(usageService.getSummary(datasetId));
	}

	@GetMapping("/analyses")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetAnalysisHistoryResponse>> getAnalyses(
			@PathVariable UUID datasetId) {
		return ResponseEntity.ok(usageService.getAnalysesHistory(datasetId));
	}

	@GetMapping("/exports")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetExportHistoryResponse>> getExports(
			@PathVariable UUID datasetId) {
		return ResponseEntity.ok(usageService.getExportsHistory(datasetId));
	}
}