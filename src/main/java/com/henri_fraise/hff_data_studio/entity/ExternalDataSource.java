package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.DataSourceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "external_data_source")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalDataSource {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "source_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 30)
	private DataSourceType type;

	@Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
	private String configJson;

	@Column(name = "credentials_encrypted", columnDefinition = "TEXT")
	private String credentialsEncrypted;

	@Column(name = "last_test_at")
	private LocalDateTime lastTestAt;

	@Column(name = "last_test_success")
	private Boolean lastTestSuccess;

	@Column(name = "last_error", columnDefinition = "TEXT")
	private String lastError;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}