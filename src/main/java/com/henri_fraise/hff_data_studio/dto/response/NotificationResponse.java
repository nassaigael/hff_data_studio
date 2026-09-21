package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
	private UUID notificationId;
	private NotificationType type;
	private String title;
	private String message;
	private String linkUrl;
	private Boolean isRead;
	private LocalDateTime readAt;
	private LocalDateTime createdAt;
}