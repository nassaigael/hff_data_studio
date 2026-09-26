package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "tenant")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "tenant_id")
	private UUID id;

	@Column(name = "code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "domain", length = 200)
	private String domain;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "max_users")
	private Integer maxUsers;

	@Column(name = "max_storage_mb")
	private Long maxStorageMb;

	@Column(name = "settings_json", columnDefinition = "TEXT")
	private String settingsJson;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}