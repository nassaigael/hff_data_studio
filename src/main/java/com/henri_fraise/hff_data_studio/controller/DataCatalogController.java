package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.CatalogEntryRequest;
import com.henri_fraise.hff_data_studio.dto.request.GlossaryTermRequest;
import com.henri_fraise.hff_data_studio.dto.response.CatalogEntryResponse;
import com.henri_fraise.hff_data_studio.dto.response.GlossaryTermResponse;
import com.henri_fraise.hff_data_studio.service.DataCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class DataCatalogController {

	private final DataCatalogService catalogService;

	@PostMapping("/entries")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CatalogEntryResponse> createEntry(
			@Valid @RequestBody CatalogEntryRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(catalogService.createEntry(request));
	}

	@PutMapping("/entries/{entryId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CatalogEntryResponse> updateEntry(
			@PathVariable UUID entryId, @Valid @RequestBody CatalogEntryRequest request) {
		return ResponseEntity.ok(catalogService.updateEntry(entryId, request));
	}

	@GetMapping("/entries")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<CatalogEntryResponse>> searchEntries(
			@RequestParam String entityType, @RequestParam UUID entityId) {
		return ResponseEntity.ok(catalogService.searchEntries(entityType, entityId));
	}

	@DeleteMapping("/entries/{entryId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> deleteEntry(@PathVariable UUID entryId) {
		catalogService.deleteEntry(entryId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/glossary")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<GlossaryTermResponse> createTerm(
			@Valid @RequestBody GlossaryTermRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(catalogService.createTerm(request));
	}

	@GetMapping("/glossary")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<GlossaryTermResponse>> searchTerms(
			@RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(catalogService.searchTerms(keyword));
	}

	@DeleteMapping("/glossary/{termId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> deleteTerm(@PathVariable UUID termId) {
		catalogService.deleteTerm(termId);
		return ResponseEntity.noContent().build();
	}
}