package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.ApprovalRequestDto;
import com.henri_fraise.hff_data_studio.dto.response.ApprovalResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.ApprovalService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

	private final ApprovalService approvalService;
	private final PageMapper pageMapper;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApprovalResponse> create(
			@Valid @RequestBody ApprovalRequestDto request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(approvalService.create(request));
	}

	@PatchMapping("/{approvalId}/approve")
	@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
	public ResponseEntity<ApprovalResponse> approve(@PathVariable UUID approvalId,
	                                                @RequestBody(required = false) Map<String, String> body) {
		String comment = body != null ? body.get("comment") : null;
		return ResponseEntity.ok(approvalService.approve(approvalId, comment));
	}

	@PatchMapping("/{approvalId}/reject")
	@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
	public ResponseEntity<ApprovalResponse> reject(@PathVariable UUID approvalId,
	                                               @RequestBody(required = false) Map<String, String> body) {
		String comment = body != null ? body.get("comment") : null;
		return ResponseEntity.ok(approvalService.reject(approvalId, comment));
	}

	@GetMapping("/mine")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PageResponse<ApprovalResponse>> mine(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(pageMapper.toPageResponse(
				approvalService.listMine(pageable), a -> a));
	}

	@GetMapping("/pending")
	@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
	public ResponseEntity<PageResponse<ApprovalResponse>> pending(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(pageMapper.toPageResponse(
				approvalService.listPending(pageable), a -> a));
	}

	@GetMapping("/entity")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ApprovalResponse>> forEntity(
			@RequestParam String entityType, @RequestParam UUID entityId) {
		return ResponseEntity.ok(approvalService.listForEntity(entityType, entityId));
	}
}