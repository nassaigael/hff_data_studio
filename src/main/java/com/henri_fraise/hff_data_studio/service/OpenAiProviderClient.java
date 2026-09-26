package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.AiMessage;
import com.henri_fraise.hff_data_studio.enums.AiRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiProviderClient implements AiProviderClient {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${ai.openai.api-key:}")
	private String apiKey;

	@Value("${ai.openai.model:gpt-4o-mini}")
	private String model;

	@Value("${ai.openai.url:https://api.openai.com/v1/chat/completions}")
	private String url;

	@Override
	public String complete(String systemPrompt, List<AiMessage> history) {
		if (apiKey == null || apiKey.isBlank()) {
			return fallbackResponse(history);
		}

		try {
			List<Map<String, String>> messages = new ArrayList<>();
			messages.add(Map.of("role", "system", "content", systemPrompt));
			for (AiMessage m : history) {
				messages.add(Map.of(
						"role", m.getRole() == AiRole.ASSISTANT ? "assistant" : "user",
						"content", m.getContent()));
			}

			Map<String, Object> body = Map.of(
					"model", model,
					"messages", messages,
					"temperature", 0.3,
					"max_tokens", 1500);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(apiKey);

			ResponseEntity<Map> response = restTemplate.exchange(
					url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

			Map<String, Object> responseBody = response.getBody();
			if (responseBody == null) return "No response from AI";
			List<Map<String, Object>> choices =
					(List<Map<String, Object>>) responseBody.get("choices");
			if (choices == null || choices.isEmpty()) return "No response from AI";
			Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
			return (String) message.get("content");
		} catch (Exception e) {
			log.error("AI call failed: {}", e.getMessage());
			return fallbackResponse(history);
		}
	}

	private String fallbackResponse(List<AiMessage> history) {
		return "AI assistant is not configured. Please set ai.openai.api-key. "
				+ "You asked: " + (history.isEmpty() ? "" : history.get(history.size() - 1).getContent());
	}
}