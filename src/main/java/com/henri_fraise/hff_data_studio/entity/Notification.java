package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.NotificationChannel;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "notification", indexes = {
		@Index(name = "idx_notif_user_read", columnList = "user_id,is_read"),
		@Index(name = "idx_notif_created", columnList = "created_at")
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "notification_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private NotificationType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 20)
	@Builder.Default
	private NotificationChannel channel = NotificationChannel.IN_APP;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "link_url", length = 500)
	private String linkUrl;

	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private Boolean isRead = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Column(name = "email_sent", nullable = false)
	@Builder.Default
	private Boolean emailSent = false;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}