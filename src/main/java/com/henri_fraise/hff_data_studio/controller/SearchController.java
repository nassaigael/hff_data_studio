package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.SearchResponse;
import com.henri_fraise.hff_data_studio.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<SearchResponse> search(
			@RequestParam String q,
			@RequestParam(required = false) List<String> entityTypes,
			@RequestParam(defaultValue = "20") int limit) {
		return ResponseEntity.ok(searchService.search(q, entityTypes, limit));
	}
}