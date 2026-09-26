package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.WebhookProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
	private UUID webhookId;
	private String name;
	private String url;
	private WebhookProvider provider;
	private List<String> eventTypes;
	private Boolean isActive;
	private Long failureCount;
	private LocalDateTime lastTriggeredAt;
	private Integer lastStatusCode;
	private LocalDateTime createdAt;
}