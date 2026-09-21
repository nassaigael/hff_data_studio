package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "dataset_version")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatasetVersion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "version_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dataset_id", nullable = false)
	private Dataset dataset;

	@Column(name = "version_number", nullable = false)
	private Integer versionNumber;

	@Column(name = "label", length = 200)
	private String label;

	@Column(name = "row_count", nullable = false)
	private Integer rowCount;

	@Column(name = "column_count", nullable = false)
	private Integer columnCount;

	@Column(name = "quality_score")
	private Double qualityScore;

	@Column(name = "snapshot_path", length = 1000)
	private String snapshotPath;

	@Column(name = "schema_hash", length = 64)
	private String schemaHash;

	@Column(name = "data_hash", length = 64)
	private String dataHash;

	@Column(name = "is_current", nullable = false)
	@Builder.Default
	private Boolean isCurrent = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}