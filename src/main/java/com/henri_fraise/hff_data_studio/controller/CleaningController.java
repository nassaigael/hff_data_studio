package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.CleaningRuleRequest;
import com.henri_fraise.hff_data_studio.dto.response.CleaningHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.CleaningService;
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
public class CleaningController {

	private final CleaningService cleaningService;
	private final PageMapper pageMapper;

	@GetMapping("/columns/{columnId}/rules")
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<List<CleaningRuleResponse>> getRulesByColumn(
			@PathVariable UUID columnId) {
		return ResponseEntity.ok(cleaningService.getRulesByColumn(columnId));
	}

	@PostMapping("/columns/{columnId}/rules")
	@PreAuthorize("hasAuthority('CLEANING_RULE_CREATE')")
	public ResponseEntity<CleaningRuleResponse> createRule(
			@PathVariable UUID columnId,
			@Valid @RequestBody CleaningRuleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(cleaningService.createRule(columnId, request));
	}

	@PutMapping("/rules/{ruleId}")
	@PreAuthorize("hasAuthority('CLEANING_RULE_UPDATE')")
	public ResponseEntity<CleaningRuleResponse> updateRule(
			@PathVariable UUID ruleId,
			@Valid @RequestBody CleaningRuleRequest request) {
		return ResponseEntity.ok(cleaningService.updateRule(ruleId, request));
	}

	@DeleteMapping("/rules/{ruleId}")
	@PreAuthorize("hasAuthority('CLEANING_RULE_DELETE')")
	public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
		cleaningService.deleteRule(ruleId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/datasets/{datasetId}/cleaning/execute")
	@PreAuthorize("hasAuthority('CLEANING_EXECUTE')")
	public ResponseEntity<Map<String, Object>> executeCleaning(
			@PathVariable UUID datasetId) {
		return ResponseEntity.accepted()
				.body(cleaningService.executeCleaning(datasetId));
	}

	@GetMapping("/datasets/{datasetId}/cleaning-history")
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<PageResponse<CleaningHistoryResponse>> getCleaningHistory(
			@PathVariable UUID datasetId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<CleaningHistoryResponse> history = cleaningService.getCleaningHistory(datasetId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(history, h -> h));
	}

	@GetMapping("/cleaning-history/{historyId}")
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<CleaningHistoryResponse> getCleaningHistoryById(
			@PathVariable UUID historyId) {
		return ResponseEntity.ok(cleaningService.getCleaningHistoryById(historyId));
	}

	@GetMapping("/datasets/{datasetId}/cleaning/status")
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<Map<String, Object>> getCleaningStatus(
			@PathVariable UUID datasetId) {
		return ResponseEntity.ok(cleaningService.getCleaningStatus(datasetId));
	}
}