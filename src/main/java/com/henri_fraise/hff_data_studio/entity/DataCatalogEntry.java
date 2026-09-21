package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "data_catalog_entry")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataCatalogEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "entry_id")
	private UUID id;

	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;

	@Column(name = "display_name", nullable = false, length = 200)
	private String displayName;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "business_domain", length = 100)
	private String businessDomain;

	@Column(name = "owner_name", length = 200)
	private String ownerName;

	@Column(name = "steward_name", length = 200)
	private String stewardName;

	@Column(name = "classification", length = 50)
	private String classification;

	@Column(name = "sensitivity_level", length = 20)
	private String sensitivityLevel;

	@Column(name = "tags", columnDefinition = "TEXT")
	private String tags;

	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}