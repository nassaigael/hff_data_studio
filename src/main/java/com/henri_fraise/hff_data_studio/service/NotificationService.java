package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.NotificationResponse;
import com.henri_fraise.hff_data_studio.entity.Notification;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.NotificationChannel;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import com.henri_fraise.hff_data_studio.mapper.NotificationMapper;
import com.henri_fraise.hff_data_studio.repository.NotificationRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final NotificationMapper notificationMapper;
	private final EmailService emailService;
	private final SecurityUtils securityUtils;

	@Async
	@Transactional
	public void send(UUID userId, NotificationType type, String title, String message, String linkUrl) {
		userRepository.findById(userId).ifPresent(user ->
				send(user, type, title, message, linkUrl));
	}

	@Async
	@Transactional
	public void send(User user, NotificationType type, String title, String message, String linkUrl) {
		Notification notification = Notification.builder()
				.user(user)
				.type(type)
				.channel(NotificationChannel.IN_APP)
				.title(title)
				.message(message)
				.linkUrl(linkUrl)
				.build();
		notificationRepository.save(notification);
		log.debug("Notification sent to {}: {}", user.getEmail(), title);
	}

	@Transactional
	public void sendWithEmail(User user, NotificationType type, String title,
	                          String message, String linkUrl) {
		send(user, type, title, message, linkUrl);
		emailService.send(user.getEmail(), title, message);
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getUserNotifications(Pageable pageable) {
		UUID userId = securityUtils.getCurrentUserId();
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
				.map(notificationMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getUnreadNotifications(Pageable pageable) {
		UUID userId = securityUtils.getCurrentUserId();
		return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
				.map(notificationMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public long countUnread() {
		return notificationRepository.countByUserIdAndIsReadFalse(securityUtils.getCurrentUserId());
	}

	@Transactional
	public void markAsRead(UUID notificationId) {
		notificationRepository.findById(notificationId).ifPresent(n -> {
			if (n.getUser().getId().equals(securityUtils.getCurrentUserId())) {
				n.setIsRead(true);
				n.setReadAt(LocalDateTime.now());
				notificationRepository.save(n);
			}
		});
	}

	@Transactional
	public int markAllAsRead() {
		return notificationRepository.markAllAsRead(
				securityUtils.getCurrentUserId(), LocalDateTime.now());
	}

	@Transactional
	public void deleteNotification(UUID notificationId) {
		notificationRepository.findById(notificationId).ifPresent(n -> {
			if (n.getUser().getId().equals(securityUtils.getCurrentUserId())) {
				notificationRepository.delete(n);
			}
		});
	}
}