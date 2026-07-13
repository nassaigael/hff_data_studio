package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredefinedAnalysisResponse {
	private UUID analysisId;
	private String analysisName;
	private String description;
	private AnalysisCategory category;
	private String categoryLabel;
	private String referenceScript;
	private Map<String, Object> requiredParametersJson;
	private Long executionCount;
}
