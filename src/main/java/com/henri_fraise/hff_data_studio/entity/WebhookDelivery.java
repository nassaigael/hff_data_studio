package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.DeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "webhook_delivery")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebhookDelivery {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "delivery_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "webhook_id", nullable = false)
	private Webhook webhook;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private DeliveryStatus status;

	@Column(name = "status_code")
	private Integer statusCode;

	@Column(name = "response_body", columnDefinition = "TEXT")
	private String responseBody;

	@Column(name = "attempt_count", nullable = false)
	@Builder.Default
	private Integer attemptCount = 0;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}