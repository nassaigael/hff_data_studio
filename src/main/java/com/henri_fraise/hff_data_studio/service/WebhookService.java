package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.request.WebhookRequest;
import com.henri_fraise.hff_data_studio.dto.response.WebhookResponse;
import com.henri_fraise.hff_data_studio.entity.Webhook;
import com.henri_fraise.hff_data_studio.entity.WebhookDelivery;
import com.henri_fraise.hff_data_studio.enums.DeliveryStatus;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.WebhookDeliveryRepository;
import com.henri_fraise.hff_data_studio.repository.WebhookRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

	private final WebhookRepository webhookRepository;
	private final WebhookDeliveryRepository deliveryRepository;
	private final SecurityUtils securityUtils;
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate = new RestTemplate();

	@Transactional
	public WebhookResponse create(WebhookRequest request) {
		Webhook webhook = Webhook.builder()
				.name(request.getName())
				.url(request.getUrl())
				.provider(request.getProvider())
				.eventTypes(String.join(",", request.getEventTypes()))
				.secret(request.getSecret())
				.isActive(request.getIsActive() != null ? request.getIsActive() : true)
				.user(securityUtils.getCurrentUser())
				.build();
		return toResponse(webhookRepository.save(webhook));
	}

	@Transactional
	public WebhookResponse update(UUID webhookId, WebhookRequest request) {
		Webhook webhook = getEntity(webhookId);
		webhook.setName(request.getName());
		webhook.setUrl(request.getUrl());
		webhook.setProvider(request.getProvider());
		webhook.setEventTypes(String.join(",", request.getEventTypes()));
		if (request.getSecret() != null) webhook.setSecret(request.getSecret());
		if (request.getIsActive() != null) webhook.setIsActive(request.getIsActive());
		return toResponse(webhookRepository.save(webhook));
	}

	@Transactional(readOnly = true)
	public List<WebhookResponse> list() {
		return webhookRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId())
				.stream().map(this::toResponse).toList();
	}

	@Transactional
	public void delete(UUID webhookId) {
		webhookRepository.delete(getEntity(webhookId));
	}

	@Async
	@Transactional
	public void dispatch(String eventType, Map<String, Object> payload) {
		webhookRepository.findByIsActiveTrue().stream()
				.filter(w -> matchesEvent(w, eventType))
				.forEach(w -> deliver(w, eventType, payload));
	}

	@Async
	@Transactional
	public void deliver(Webhook webhook, String eventType, Map<String, Object> payload) {
		String json;
		try {
			json = objectMapper.writeValueAsString(payload);
		} catch (Exception e) {
			json = payload.toString();
		}

		WebhookDelivery delivery = WebhookDelivery.builder()
				.webhook(webhook)
				.eventType(eventType)
				.payload(json)
				.status(DeliveryStatus.PENDING)
				.build();
		deliveryRepository.save(delivery);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
				headers.add("X-HFF-Signature", hmac(json, webhook.getSecret()));
			}
			HttpEntity<String> entity = new HttpEntity<>(json, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(
					webhook.getUrl(), entity, String.class);

			delivery.setStatusCode(response.getStatusCode().value());
			delivery.setResponseBody(response.getBody());
			delivery.setStatus(response.getStatusCode().is2xxSuccessful()
					? DeliveryStatus.SUCCESS : DeliveryStatus.FAILED);
			delivery.setAttemptCount(delivery.getAttemptCount() + 1);
			webhook.setLastStatusCode(response.getStatusCode().value());
		} catch (Exception e) {
			delivery.setStatus(DeliveryStatus.FAILED);
			delivery.setErrorMessage(e.getMessage());
			delivery.setAttemptCount(delivery.getAttemptCount() + 1);
			webhook.setFailureCount(webhook.getFailureCount() + 1);
			log.error("Webhook delivery failed: {}", e.getMessage());
		}

		webhook.setLastTriggeredAt(LocalDateTime.now());
		webhookRepository.save(webhook);
		deliveryRepository.save(delivery);
	}

	private boolean matchesEvent(Webhook webhook, String eventType) {
		return Arrays.stream(webhook.getEventTypes().split(","))
				.map(String::trim)
				.anyMatch(e -> e.equals(eventType) || e.equals("*"));
	}

	private String hmac(String data, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
		byte[] hash = mac.doFinal(data.getBytes());
		StringBuilder hex = new StringBuilder();
		for (byte b : hash) hex.append(String.format("%02x", b));
		return hex.toString();
	}

	private Webhook getEntity(UUID id) {
		return webhookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Webhook not found"));
	}

	private WebhookResponse toResponse(Webhook w) {
		return WebhookResponse.builder()
				.webhookId(w.getId())
				.name(w.getName())
				.url(w.getUrl())
				.provider(w.getProvider())
				.eventTypes(Arrays.asList(w.getEventTypes().split(",")))
				.isActive(w.getIsActive())
				.failureCount(w.getFailureCount())
				.lastTriggeredAt(w.getLastTriggeredAt())
				.lastStatusCode(w.getLastStatusCode())
				.createdAt(w.getCreatedAt())
				.build();
	}
}