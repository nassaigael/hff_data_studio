package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "dashboard")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dashboard {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "dashboard_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "is_default", nullable = false)
	@Builder.Default
	private Boolean isDefault = false;

	@Column(name = "is_public", nullable = false)
	@Builder.Default
	private Boolean isPublic = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC")
	private List<DashboardWidget> widgets;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}