package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.NotificationResponse;
import com.henri_fraise.hff_data_studio.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

	public NotificationResponse toResponse(Notification n) {
		if (n == null) return null;
		return NotificationResponse.builder()
				.notificationId(n.getId())
				.type(n.getType())
				.title(n.getTitle())
				.message(n.getMessage())
				.linkUrl(n.getLinkUrl())
				.isRead(n.getIsRead())
				.readAt(n.getReadAt())
				.createdAt(n.getCreatedAt())
				.build();
	}
}