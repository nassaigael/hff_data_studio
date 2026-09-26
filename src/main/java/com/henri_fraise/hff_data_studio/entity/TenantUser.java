package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "tenant_user",
		uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "user_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "tenant_user_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "role", length = 50)
	@Builder.Default
	private String role = "MEMBER";

	@Column(name = "is_owner", nullable = false)
	@Builder.Default
	private Boolean isOwner = false;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private LocalDateTime joinedAt;
}