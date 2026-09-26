package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CommentResponse;
import com.henri_fraise.hff_data_studio.entity.Comment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

	public CommentResponse toResponse(Comment comment) {
		return toResponse(comment, false);
	}

	public CommentResponse toResponse(Comment comment, boolean withReplies) {
		if (comment == null) return null;

		List<CommentResponse> replies = null;
		if (withReplies && comment.getReplies() != null) {
			replies = comment.getReplies().stream()
					.filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
					.map(r -> toResponse(r, true))
					.toList();
		}

		return CommentResponse.builder()
				.commentId(comment.getId())
				.content(comment.getContent())
				.entityType(comment.getEntityType())
				.entityId(comment.getEntityId())
				.authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
				.authorFullName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : null)
				.authorEmail(comment.getAuthor() != null ? comment.getAuthor().getEmail() : null)
				.parentId(comment.getParent() != null ? comment.getParent().getId() : null)
				.replies(replies)
				.isEdited(comment.getIsEdited())
				.createdAt(comment.getCreatedAt())
				.updatedAt(comment.getUpdatedAt())
				.build();
	}
}