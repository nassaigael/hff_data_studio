package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ScheduleFrequency;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledAnalysisResponse {
	private UUID scheduleId;
	private String name;
	private UUID datasetId;
	private String datasetName;
	private UUID analysisId;
	private String analysisName;
	private ScheduleFrequency frequency;
	private String cronExpression;
	private LocalDateTime nextRunAt;
	private LocalDateTime lastRunAt;
	private Boolean isActive;
	private Long runCount;
	private Long failureCount;
	private LocalDateTime createdAt;
}