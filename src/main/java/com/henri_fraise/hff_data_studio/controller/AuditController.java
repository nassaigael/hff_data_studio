package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.AuditLogResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.AuditLogService;
import com.henri_fraise.hff_data_studio.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AuditController {

	private final AuditLogService auditService;
	private final PageMapper pageMapper;

	@GetMapping
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
			@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(required = false) UUID userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
			@RequestParam(required = false) String action) {
		Page<AuditLogResponse> logs = auditService.getAuditLogs(pageable, userId, startDate, endDate, action);
		return ResponseEntity.ok(pageMapper.toPageResponse(logs, log -> log));
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogsByUser(
			@PathVariable UUID userId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<AuditLogResponse> logs = auditService.getAuditLogsByUser(userId, pageable);
		return ResponseEntity.ok(pageMapper.toPageResponse(logs, log -> log));
	}

	@GetMapping("/{logId}")
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable UUID logId) {
		return ResponseEntity.ok(auditService.getAuditLogById(logId));
	}

	@GetMapping("/actions")
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ResponseEntity<List<String>> getActions() {
		return ResponseEntity.ok(auditService.getActions());
	}

	@GetMapping("/summary")
	@PreAuthorize("hasAuthority('AUDIT_VIEW')")
	public ResponseEntity<Map<String, Object>> getAuditSummary() {
		return ResponseEntity.ok(auditService.getAuditSummary());
	}
}