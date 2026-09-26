package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CommentRequest;
import com.henri_fraise.hff_data_studio.dto.response.CommentResponse;
import com.henri_fraise.hff_data_studio.entity.Comment;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import com.henri_fraise.hff_data_studio.exception.ForbiddenException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.CommentMapper;
import com.henri_fraise.hff_data_studio.repository.CommentRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

	private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w.\\-]+@[\\w.\\-]+)");

	private final CommentRepository commentRepository;
	private final CommentMapper commentMapper;
	private final NotificationService notificationService;
	private final SecurityUtils securityUtils;
	private final com.henri_fraise.hff_data_studio.repository.UserRepository userRepository;

	@Transactional
	public CommentResponse create(CommentRequest request) {
		User author = securityUtils.getCurrentUser();

		Comment parent = null;
		if (request.getParentId() != null) {
			parent = commentRepository.findById(request.getParentId())
					.orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
		}

		Comment comment = Comment.builder()
				.content(request.getContent())
				.entityType(request.getEntityType())
				.entityId(request.getEntityId())
				.author(author)
				.parent(parent)
				.build();

		Comment saved = commentRepository.save(comment);
		processMentions(saved);
		notifyParentAuthor(saved, author);

		return commentMapper.toResponse(saved, true);
	}

	@Transactional
	public CommentResponse update(UUID commentId, String content) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
		User currentUser = securityUtils.getCurrentUser();

		if (!comment.getAuthor().getId().equals(currentUser.getId()) && !securityUtils.isAdmin()) {
			throw new ForbiddenException("You can only edit your own comments");
		}

		comment.setContent(content);
		comment.setIsEdited(true);
		return commentMapper.toResponse(commentRepository.save(comment), true);
	}

	@Transactional
	public void delete(UUID commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
		User currentUser = securityUtils.getCurrentUser();

		if (!comment.getAuthor().getId().equals(currentUser.getId()) && !securityUtils.isAdmin()) {
			throw new ForbiddenException("You can only delete your own comments");
		}

		comment.setIsDeleted(true);
		commentRepository.save(comment);
	}

	@Transactional(readOnly = true)
	public Page<CommentResponse> list(String entityType, UUID entityId, Pageable pageable) {
		return commentRepository
				.findByEntityTypeAndEntityIdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(
						entityType, entityId, pageable)
				.map(c -> commentMapper.toResponse(c, true));
	}

	@Transactional(readOnly = true)
	public long count(String entityType, UUID entityId) {
		return commentRepository.countByEntityTypeAndEntityIdAndIsDeletedFalse(entityType, entityId);
	}

	private void processMentions(Comment comment) {
		Matcher matcher = MENTION_PATTERN.matcher(comment.getContent());
		while (matcher.find()) {
			String email = matcher.group(1);
			userRepository.findByEmail(email).ifPresent(user -> {
				if (!user.getId().equals(comment.getAuthor().getId())) {
					notificationService.send(
							user.getId(),
							NotificationType.MENTION,
							"You were mentioned",
							comment.getAuthor().getFullName() + " mentioned you in a comment",
							"/comments/" + comment.getId());
				}
			});
		}
	}

	private void notifyParentAuthor(Comment comment, User author) {
		if (comment.getParent() != null
				&& !comment.getParent().getAuthor().getId().equals(author.getId())) {
			notificationService.send(
					comment.getParent().getAuthor().getId(),
					NotificationType.INFO,
					"New reply",
					author.getFullName() + " replied to your comment",
					"/comments/" + comment.getId());
		}
	}
}