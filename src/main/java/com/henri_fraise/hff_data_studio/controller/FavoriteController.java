package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.FavoriteRequest;
import com.henri_fraise.hff_data_studio.dto.response.FavoriteResponse;
import com.henri_fraise.hff_data_studio.service.UserActivityService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

	private final UserActivityService activityService;

	@PostMapping("/toggle")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<FavoriteResponse> toggle(@Valid @RequestBody FavoriteRequest request) {
		return ResponseEntity.ok(activityService.toggleFavorite(request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<FavoriteResponse>> list() {
		return ResponseEntity.ok(activityService.listFavorites());
	}

	@GetMapping("/check")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Boolean> check(@RequestParam String entityType,
	                                     @RequestParam UUID entityId) {
		return ResponseEntity.ok(activityService.isFavorite(entityType, entityId));
	}
}