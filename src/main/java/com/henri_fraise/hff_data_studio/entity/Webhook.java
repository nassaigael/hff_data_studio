package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.WebhookProvider;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "webhook")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Webhook {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "webhook_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "url", nullable = false, length = 1000)
	private String url;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	private WebhookProvider provider;

	@Column(name = "event_types", nullable = false, columnDefinition = "TEXT")
	private String eventTypes;

	@Column(name = "secret", length = 200)
	private String secret;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "failure_count", nullable = false)
	@Builder.Default
	private Long failureCount = 0L;

	@Column(name = "last_triggered_at")
	private LocalDateTime lastTriggeredAt;

	@Column(name = "last_status_code")
	private Integer lastStatusCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}