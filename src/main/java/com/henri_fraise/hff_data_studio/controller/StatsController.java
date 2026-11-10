package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.StatsResponse;
import com.henri_fraise.hff_data_studio.service.StatsService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Validated
public class StatsController {

	private final StatsService statsService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<StatsResponse> getStats(
			@RequestParam(name = "projectId", required = false)
			@Pattern(
					regexp = "all|[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
					message = "projectId must be 'all' or a valid UUID")
			String projectId) {
		return ResponseEntity.ok(statsService.getStats(projectId));
	}
}