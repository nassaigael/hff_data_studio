package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.CleaningSuggestionResponse;
import com.henri_fraise.hff_data_studio.service.CleaningSuggestionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datasets/{datasetId}/cleaning-suggestions")
@RequiredArgsConstructor
public class CleaningSuggestionController {

	private final CleaningSuggestionService suggestionService;

	@GetMapping
	@PreAuthorize("hasAuthority('CLEANING_VIEW')")
	public ResponseEntity<List<CleaningSuggestionResponse>> suggest(
			@PathVariable UUID datasetId) {
		return ResponseEntity.ok(suggestionService.suggestForDataset(datasetId));
	}
}