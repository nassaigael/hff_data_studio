package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ScheduleFrequency;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "scheduled_analysis")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "schedule_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dataset_id", nullable = false)
	private Dataset dataset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "analysis_id")
	private PredefinedAnalysis predefinedAnalysis;

	@Column(name = "parameters_json", columnDefinition = "TEXT")
	private String parametersJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "frequency", nullable = false, length = 20)
	private ScheduleFrequency frequency;

	@Column(name = "cron_expression", length = 100)
	private String cronExpression;

	@Column(name = "next_run_at")
	private LocalDateTime nextRunAt;

	@Column(name = "last_run_at")
	private LocalDateTime lastRunAt;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "run_count", nullable = false)
	@Builder.Default
	private Long runCount = 0L;

	@Column(name = "failure_count", nullable = false)
	@Builder.Default
	private Long failureCount = 0L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}