package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.RuleType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CleaningRuleResponse {
    private UUID ruleId;
    private RuleType ruleType;
    private String ruleTypeLabel;
    private Map<String, Object> parametersJson;
    private Integer executionOrder;
    private Boolean isActive;
    private UUID columnId;
    private String columnName;
    private UUID datasetId;
    private String datasetName;
}
