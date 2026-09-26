package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.ScheduleFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledAnalysisRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Dataset ID is required")
	private UUID datasetId;

	private UUID analysisId;

	private Map<String, Object> parameters;

	@NotNull(message = "Frequency is required")
	private ScheduleFrequency frequency;

	private String cronExpression;

	private Boolean isActive;
}