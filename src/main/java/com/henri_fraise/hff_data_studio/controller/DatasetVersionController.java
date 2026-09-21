package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.DatasetVersionResponse;
import com.henri_fraise.hff_data_studio.dto.response.VersionDiffResponse;
import com.henri_fraise.hff_data_studio.service.DatasetVersionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datasets/{datasetId}/versions")
@RequiredArgsConstructor
public class DatasetVersionController {

	private final DatasetVersionService versionService;

	@PostMapping
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<DatasetVersionResponse> create(
			@PathVariable UUID datasetId,
			@RequestParam(required = false) String label) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(versionService.createVersion(datasetId, label));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<DatasetVersionResponse>> list(@PathVariable UUID datasetId) {
		return ResponseEntity.ok(versionService.listVersions(datasetId));
	}

	@GetMapping("/diff")
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<VersionDiffResponse> diff(
			@PathVariable UUID datasetId,
			@RequestParam Integer from,
			@RequestParam Integer to) {
		return ResponseEntity.ok(versionService.diff(datasetId, from, to));
	}
}