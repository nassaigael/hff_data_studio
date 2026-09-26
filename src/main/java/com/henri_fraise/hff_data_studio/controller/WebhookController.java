package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.WebhookRequest;
import com.henri_fraise.hff_data_studio.dto.response.WebhookResponse;
import com.henri_fraise.hff_data_studio.service.WebhookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

	private final WebhookService webhookService;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<WebhookResponse> create(@Valid @RequestBody WebhookRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.create(request));
	}

	@PutMapping("/{webhookId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<WebhookResponse> update(@PathVariable UUID webhookId,
	                                              @Valid @RequestBody WebhookRequest request) {
		return ResponseEntity.ok(webhookService.update(webhookId, request));
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<WebhookResponse>> list() {
		return ResponseEntity.ok(webhookService.list());
	}

	@DeleteMapping("/{webhookId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID webhookId) {
		webhookService.delete(webhookId);
		return ResponseEntity.noContent().build();
	}
}