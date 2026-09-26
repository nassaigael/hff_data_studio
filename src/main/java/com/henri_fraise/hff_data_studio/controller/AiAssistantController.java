package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.request.AiMessageRequest;
import com.henri_fraise.hff_data_studio.dto.response.AiConversationResponse;
import com.henri_fraise.hff_data_studio.service.AiAssistantService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiAssistantController {

	private final AiAssistantService aiService;

	@PostMapping("/chat")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<AiConversationResponse> chat(@Valid @RequestBody AiMessageRequest request) {
		return ResponseEntity.ok(aiService.chat(request));
	}

	@GetMapping("/conversations")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<AiConversationResponse>> list() {
		return ResponseEntity.ok(aiService.listConversations());
	}

	@GetMapping("/conversations/{conversationId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<AiConversationResponse> get(@PathVariable UUID conversationId) {
		return ResponseEntity.ok(aiService.getConversation(conversationId));
	}

	@DeleteMapping("/conversations/{conversationId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID conversationId) {
		aiService.deleteConversation(conversationId);
		return ResponseEntity.noContent().build();
	}
}