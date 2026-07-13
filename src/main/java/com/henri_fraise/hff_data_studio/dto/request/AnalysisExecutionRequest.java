package com.henri_fraise.hff_data_studio.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisExecutionRequest {

    @NotNull(message = "Dataset ID is required")
    private UUID datasetId;

    private UUID analysisId;

    private Map<String, Object> parameters;
}
