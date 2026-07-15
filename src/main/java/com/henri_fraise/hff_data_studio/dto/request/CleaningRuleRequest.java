package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.RuleType;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CleaningRuleRequest {

  @NotNull(message = "Rule type is required")
  private RuleType ruleType;

  private Map<String, Object> parametersJson;

  private Integer executionOrder;

  @Builder.Default private Boolean isActive = true;
}
