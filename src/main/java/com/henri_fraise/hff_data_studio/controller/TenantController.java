package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.TenantCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.TenantResponse;
import com.henri_fraise.hff_data_studio.service.TenantService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

	private final TenantService tenantService;

	@PostMapping
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<TenantResponse> create(
			@Valid @RequestBody TenantCreationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.create(request));
	}

	@GetMapping("/current")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<TenantResponse> current() {
		return ResponseEntity.ok(tenantService.getCurrent());
	}

	@GetMapping("/mine")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<TenantResponse>> mine() {
		return ResponseEntity.ok(tenantService.listMine());
	}

	@PostMapping("/{tenantId}/users/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Void> addUser(@PathVariable UUID tenantId,
	                                    @PathVariable UUID userId,
	                                    @RequestParam String role) {
		tenantService.addUser(tenantId, userId, role);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{tenantId}/users/{userId}")
	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<Void> removeUser(@PathVariable UUID tenantId, @PathVariable UUID userId) {
		tenantService.removeUser(tenantId, userId);
		return ResponseEntity.noContent().build();
	}
}