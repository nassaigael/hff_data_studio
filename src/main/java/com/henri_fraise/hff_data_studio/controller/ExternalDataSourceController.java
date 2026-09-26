package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ExternalDataSourceRequest;
import com.henri_fraise.hff_data_studio.dto.response.ExternalDataSourceResponse;
import com.henri_fraise.hff_data_studio.service.ExternalDataSourceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data-sources")
@RequiredArgsConstructor
public class ExternalDataSourceController {

	private final ExternalDataSourceService dataSourceService;

	@PostMapping
	@PreAuthorize("hasAuthority('FILE_UPLOAD')")
	public ResponseEntity<ExternalDataSourceResponse> create(
			@Valid @RequestBody ExternalDataSourceRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dataSourceService.create(request));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('FILE_VIEW')")
	public ResponseEntity<List<ExternalDataSourceResponse>> list() {
		return ResponseEntity.ok(dataSourceService.list());
	}

	@PostMapping("/{sourceId}/test")
	@PreAuthorize("hasAuthority('FILE_UPLOAD')")
	public ResponseEntity<Boolean> test(@PathVariable UUID sourceId) {
		return ResponseEntity.ok(dataSourceService.testConnection(sourceId));
	}

	@DeleteMapping("/{sourceId}")
	@PreAuthorize("hasAuthority('FILE_DELETE')")
	public ResponseEntity<Void> delete(@PathVariable UUID sourceId) {
		dataSourceService.delete(sourceId);
		return ResponseEntity.noContent().build();
	}
}