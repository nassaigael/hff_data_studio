package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.CommentRequest;
import com.henri_fraise.hff_data_studio.dto.response.CommentResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.CommentService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;
	private final PageMapper pageMapper;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(request));
	}

	@PutMapping("/{commentId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<CommentResponse> update(@PathVariable UUID commentId,
	                                              @RequestBody Map<String, String> body) {
		return ResponseEntity.ok(commentService.update(commentId, body.get("content")));
	}

	@DeleteMapping("/{commentId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID commentId) {
		commentService.delete(commentId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PageResponse<CommentResponse>> list(
			@RequestParam String entityType,
			@RequestParam UUID entityId,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(pageMapper.toPageResponse(
				commentService.list(entityType, entityId, pageable), c -> c));
	}

	@GetMapping("/count")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Long> count(@RequestParam String entityType,
	                                  @RequestParam UUID entityId) {
		return ResponseEntity.ok(commentService.count(entityType, entityId));
	}
}