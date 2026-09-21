package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ScheduledAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.ScheduledAnalysisResponse;
import com.henri_fraise.hff_data_studio.service.ScheduledAnalysisService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduledAnalysisController {

	private final ScheduledAnalysisService scheduleService;

	@PostMapping
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<ScheduledAnalysisResponse> create(
			@Valid @RequestBody ScheduledAnalysisRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.create(request));
	}

	@PutMapping("/{scheduleId}")
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<ScheduledAnalysisResponse> update(
			@PathVariable UUID scheduleId,
			@Valid @RequestBody ScheduledAnalysisRequest request) {
		return ResponseEntity.ok(scheduleService.update(scheduleId, request));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('ANALYSIS_VIEW')")
	public ResponseEntity<List<ScheduledAnalysisResponse>> list() {
		return ResponseEntity.ok(scheduleService.listByUser());
	}

	@DeleteMapping("/{scheduleId}")
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<Void> delete(@PathVariable UUID scheduleId) {
		scheduleService.delete(scheduleId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{scheduleId}/toggle")
	@PreAuthorize("hasAuthority('ANALYSIS_RUN')")
	public ResponseEntity<Void> toggle(@PathVariable UUID scheduleId,
	                                   @RequestParam boolean active) {
		scheduleService.toggle(scheduleId, active);
		return ResponseEntity.noContent().build();
	}
}