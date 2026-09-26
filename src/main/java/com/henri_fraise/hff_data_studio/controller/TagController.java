package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.TagRequest;
import com.henri_fraise.hff_data_studio.dto.response.TagResponse;
import com.henri_fraise.hff_data_studio.service.TagService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

	private final TagService tagService;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<TagResponse>> list() {
		return ResponseEntity.ok(tagService.list());
	}

	@DeleteMapping("/{tagId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID tagId) {
		tagService.delete(tagId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/assign")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> assign(@Valid @RequestBody TagRequest.TagAssignRequest request) {
		tagService.assign(request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{tagId}/entity")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> unassign(@PathVariable UUID tagId,
	                                     @RequestParam String entityType,
	                                     @RequestParam UUID entityId) {
		tagService.unassign(tagId, entityType, entityId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/entity")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<TagResponse>> forEntity(@RequestParam String entityType,
	                                                   @RequestParam UUID entityId) {
		return ResponseEntity.ok(tagService.getTagsForEntity(entityType, entityId));
	}
}