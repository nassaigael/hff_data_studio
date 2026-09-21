package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
	private UUID commentId;
	private String content;
	private String entityType;
	private UUID entityId;
	private UUID authorId;
	private String authorFullName;
	private String authorEmail;
	private UUID parentId;
	private List<CommentResponse> replies;
	private Boolean isEdited;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}