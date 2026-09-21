package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.DashboardRequest;
import com.henri_fraise.hff_data_studio.dto.response.DashboardResponse;
import com.henri_fraise.hff_data_studio.service.DashboardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DashboardResponse> create(@Valid @RequestBody DashboardRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(dashboardService.create(request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<DashboardResponse>> list() {
		return ResponseEntity.ok(dashboardService.list());
	}

	@GetMapping("/{dashboardId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DashboardResponse> getById(@PathVariable UUID dashboardId) {
		return ResponseEntity.ok(dashboardService.getById(dashboardId));
	}

	@PutMapping("/{dashboardId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DashboardResponse> update(@PathVariable UUID dashboardId,
	                                                @Valid @RequestBody DashboardRequest request) {
		return ResponseEntity.ok(dashboardService.update(dashboardId, request));
	}

	@DeleteMapping("/{dashboardId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID dashboardId) {
		dashboardService.delete(dashboardId);
		return ResponseEntity.noContent().build();
	}
}