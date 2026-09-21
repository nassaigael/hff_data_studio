package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.AiRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversationResponse {
	private UUID conversationId;
	private String title;
	private String contextType;
	private UUID contextId;
	private List<AiMessageResponse> messages;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AiMessageResponse {
		private UUID messageId;
		private AiRole role;
		private String content;
		private LocalDateTime createdAt;
	}
}