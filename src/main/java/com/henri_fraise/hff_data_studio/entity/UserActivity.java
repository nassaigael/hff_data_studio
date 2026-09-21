package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "user_activity", indexes = {
		@Index(name = "idx_activity_user", columnList = "user_id,created_at")
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActivity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "activity_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "action", nullable = false, length = 100)
	private String action;

	@Column(name = "entity_type", length = 50)
	private String entityType;

	@Column(name = "entity_id")
	private UUID entityId;

	@Column(name = "details", columnDefinition = "TEXT")
	private String details;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}