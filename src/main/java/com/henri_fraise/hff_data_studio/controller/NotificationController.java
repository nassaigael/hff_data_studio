package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.NotificationResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;
	private final PageMapper pageMapper;

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PageResponse<NotificationResponse>> list(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(pageMapper.toPageResponse(
				notificationService.getUserNotifications(pageable), n -> n));
	}

	@GetMapping("/unread")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PageResponse<NotificationResponse>> unread(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(pageMapper.toPageResponse(
				notificationService.getUnreadNotifications(pageable), n -> n));
	}

	@GetMapping("/count/unread")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Long> countUnread() {
		return ResponseEntity.ok(notificationService.countUnread());
	}

	@PatchMapping("/{notificationId}/read")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
		notificationService.markAsRead(notificationId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/read-all")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Integer> markAllAsRead() {
		return ResponseEntity.ok(notificationService.markAllAsRead());
	}

	@DeleteMapping("/{notificationId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> delete(@PathVariable UUID notificationId) {
		notificationService.deleteNotification(notificationId);
		return ResponseEntity.noContent().build();
	}
}