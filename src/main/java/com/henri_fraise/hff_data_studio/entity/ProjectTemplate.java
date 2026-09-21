package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "project_template")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "template_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "category", length = 100)
	private String category;

	@Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
	private String configJson;

	@Column(name = "is_public", nullable = false)
	@Builder.Default
	private Boolean isPublic = false;

	@Column(name = "usage_count", nullable = false)
	@Builder.Default
	private Long usageCount = 0L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}