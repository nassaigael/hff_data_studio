package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.AnalysisExecutionRequest;
import com.henri_fraise.hff_data_studio.dto.request.PredefinedAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisExecutionResponse;
import com.henri_fraise.hff_data_studio.dto.response.AnalysisResultResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalysisController {

	private final AnalysisService analysisService;
	private final PageMapper pageMapper;

	@GetMapping("/analyses")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<List<PredefinedAnalysisResponse>> getPredefinedAnalyses(
			@RequestParam(required = false) AnalysisCategory category) {
		return ResponseEntity.ok(analysisService.getPredefinedAnalyses(category));
	}

	@GetMapping("/analyses/{analysisId}")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<PredefinedAnalysisResponse> getPredefinedAnalysisById(
			@PathVariable UUID analysisId) {
		return ResponseEntity.ok(analysisService.getPredefinedAnalysisById(analysisId));
	}

	@PostMapping("/analyses")
	@PreAuthorize("hasAuthority('ANALYSIS_MODEL_SAVE')")
	public ResponseEntity<PredefinedAnalysisResponse> createPredefinedAnalysis(
			@Valid @RequestBody PredefinedAnalysisRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(analysisService.createPredefinedAnalysis(request));
	}

	@PutMapping("/analyses/{analysisId}")
	@PreAuthorize("hasAuthority('ANALYSIS_MODEL_SAVE')")
	public ResponseEntity<PredefinedAnalysisResponse> updatePredefinedAnalysis(
			@PathVariable UUID analysisId,
			@Valid @RequestBody PredefinedAnalysisRequest request) {
		return ResponseEntity.ok(analysisService.updatePredefinedAnalysis(analysisId, request));
	}

	@DeleteMapping("/analyses/{analysisId}")
	@PreAuthorize("hasAuthority('ANALYSIS_MODEL_SAVE')")
	public ResponseEntity<Void> deletePredefinedAnalysis(@PathVariable UUID analysisId) {
		analysisService.deletePredefinedAnalysis(analysisId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/analyses/categories")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<List<String>> getAnalysisCategories() {
		return ResponseEntity.ok(analysisService.getAllCategories());
	}

	@PostMapping("/executions")
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<AnalysisExecutionResponse> runAnalysis(
			@Valid @RequestBody AnalysisExecutionRequest request) {
		return ResponseEntity.accepted()
				.body(analysisService.runAnalysis(request));
	}

	@GetMapping("/executions")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<PageResponse<AnalysisExecutionResponse>> getExecutions(
			@RequestParam(required = false) UUID datasetId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<AnalysisExecutionResponse> executions = analysisService.getExecutions(datasetId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(executions, e -> e));
	}

	@GetMapping("/executions/{executionId}")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<AnalysisExecutionResponse> getExecutionById(
			@PathVariable UUID executionId) {
		return ResponseEntity.ok(analysisService.getExecutionById(executionId));
	}

	@GetMapping("/executions/{executionId}/results")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<List<AnalysisResultResponse>> getExecutionResults(
			@PathVariable UUID executionId) {
		return ResponseEntity.ok(analysisService.getExecutionResults(executionId));
	}

	@GetMapping("/results/{resultId}/download")
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public byte[] downloadResult(@PathVariable UUID resultId) {
		return analysisService.downloadResult(resultId);
	}

	@PostMapping("/analyses/models")
	@PreAuthorize("hasAuthority('ANALYSIS_MODEL_SAVE')")
	public ResponseEntity<Map<String, Object>> saveAnalysisModel(
			@RequestBody Map<String, Object> request) {
		return ResponseEntity.ok(analysisService.saveAnalysisModel(request));
	}

	@PatchMapping("/executions/{executionId}/cancel")
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<Void> cancelExecution(@PathVariable UUID executionId) {
		analysisService.cancelExecution(executionId);
		return ResponseEntity.noContent().build();
	}
}