package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageRequest {

	private UUID conversationId;

	@NotBlank(message = "Message is required")
	private String message;

	private String contextType;

	private UUID contextId;
}