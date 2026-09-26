package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.DryRunResponse;
import com.henri_fraise.hff_data_studio.service.CleaningDryRunService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datasets/{datasetId}/cleaning")
@RequiredArgsConstructor
public class CleaningDryRunController {

	private final CleaningDryRunService dryRunService;

	@PostMapping("/dry-run")
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<DryRunResponse> dryRun(
			@PathVariable UUID datasetId,
			@RequestBody(required = false) List<UUID> ruleIds) {
		return ResponseEntity.ok(dryRunService.dryRun(datasetId, ruleIds));
	}
}