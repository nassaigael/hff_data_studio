package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "entity_tag",
		uniqueConstraints = @UniqueConstraint(columnNames = {"tag_id", "entity_type", "entity_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityTag {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "entity_tag_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag tag;

	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}