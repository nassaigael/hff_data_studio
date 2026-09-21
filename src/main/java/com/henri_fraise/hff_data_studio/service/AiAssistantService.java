package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.AiMessageRequest;
import com.henri_fraise.hff_data_studio.dto.response.AiConversationResponse;
import com.henri_fraise.hff_data_studio.entity.AiConversation;
import com.henri_fraise.hff_data_studio.entity.AiMessage;
import com.henri_fraise.hff_data_studio.enums.AiRole;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.AiConversationRepository;
import com.henri_fraise.hff_data_studio.repository.AiMessageRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantService {

	private final AiConversationRepository conversationRepository;
	private final AiMessageRepository messageRepository;
	private final AiProviderClient aiClient;
	private final SecurityUtils securityUtils;
	private final DatasetService datasetService;

	@Transactional
	public AiConversationResponse chat(AiMessageRequest request) {
		AiConversation conversation = resolveConversation(request);

		AiMessage userMessage = AiMessage.builder()
				.conversation(conversation)
				.role(AiRole.USER)
				.content(request.getMessage())
				.build();
		messageRepository.save(userMessage);

		String systemPrompt = buildSystemPrompt(request.getContextType(), request.getContextId());
		List<AiMessage> history = messageRepository
				.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

		String response = aiClient.complete(systemPrompt, history);

		AiMessage assistantMessage = AiMessage.builder()
				.conversation(conversation)
				.role(AiRole.ASSISTANT)
				.content(response)
				.build();
		messageRepository.save(assistantMessage);

		if (conversation.getTitle() == null) {
			conversation.setTitle(truncate(request.getMessage(), 80));
			conversationRepository.save(conversation);
		}

		return toResponse(conversation);
	}

	@Transactional(readOnly = true)
	public List<AiConversationResponse> listConversations() {
		return conversationRepository.findByUserIdOrderByUpdatedAtDesc(
				securityUtils.getCurrentUserId()).stream().map(this::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public AiConversationResponse getConversation(UUID conversationId) {
		return toResponse(conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found")));
	}

	@Transactional
	public void deleteConversation(UUID conversationId) {
		conversationRepository.deleteById(conversationId);
	}

	private AiConversation resolveConversation(AiMessageRequest request) {
		if (request.getConversationId() != null) {
			return conversationRepository.findById(request.getConversationId())
					.orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
		}
		return conversationRepository.save(AiConversation.builder()
				.user(securityUtils.getCurrentUser())
				.contextType(request.getContextType())
				.contextId(request.getContextId())
				.build());
	}

	private String buildSystemPrompt(String contextType, UUID contextId) {
		StringBuilder sb = new StringBuilder();
		sb.append("You are a helpful data analyst assistant for HFF Data Studio. ");
		sb.append("Answer concisely in the user's language. ");

		if ("DATASET".equals(contextType) && contextId != null) {
			try {
				var dataset = datasetService.getDatasetEntityById(contextId);
				sb.append("Context: Dataset '").append(dataset.getDatasetName())
						.append("' with ").append(dataset.getRowCount()).append(" rows and ")
						.append(dataset.getColumnCount()).append(" columns. ");
			} catch (Exception e) {
				log.warn("Failed to load dataset context: {}", e.getMessage());
			}
		}
		return sb.toString();
	}

	private String truncate(String s, int max) {
		return s.length() > max ? s.substring(0, max) + "..." : s;
	}

	private AiConversationResponse toResponse(AiConversation c) {
		List<AiConversationResponse.AiMessageResponse> messages =
				messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId())
						.stream().map(this::toMessageResponse).toList();
		return AiConversationResponse.builder()
				.conversationId(c.getId())
				.title(c.getTitle())
				.contextType(c.getContextType())
				.contextId(c.getContextId())
				.messages(messages)
				.createdAt(c.getCreatedAt())
				.updatedAt(c.getUpdatedAt())
				.build();
	}

	private AiConversationResponse toSummary(AiConversation c) {
		return AiConversationResponse.builder()
				.conversationId(c.getId())
				.title(c.getTitle())
				.contextType(c.getContextType())
				.contextId(c.getContextId())
				.messages(new ArrayList<>())
				.createdAt(c.getCreatedAt())
				.updatedAt(c.getUpdatedAt())
				.build();
	}

	private AiConversationResponse.AiMessageResponse toMessageResponse(AiMessage m) {
		return AiConversationResponse.AiMessageResponse.builder()
				.messageId(m.getId())
				.role(m.getRole())
				.content(m.getContent())
				.createdAt(m.getCreatedAt())
				.build();
	}
}