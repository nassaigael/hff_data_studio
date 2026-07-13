package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.stereotype.Component;

@Component
public class CleaningRuleMapper {

	public CleaningRuleResponse toResponse(CleaningRule rule) {
		if (rule == null)
			return null;

		return CleaningRuleResponse.builder()
				.ruleId(rule.getId())
				.ruleType(rule.getRuleType())
				.ruleTypeLabel(getRuleTypeLabel(rule.getRuleType()))
				.parametersJson(rule.getParametersJson())
				.executionOrder(rule.getExecutionOrder())
				.isActive(rule.getIsActive())
				.columnId(rule.getColumn() != null ? rule.getColumn().getId() : null)
				.columnName(rule.getColumn() != null ? rule.getColumn().getOriginalName() : null)
				.datasetId(
						rule.getColumn() != null && rule.getColumn().getDataset() != null
								? rule.getColumn().getDataset().getId()
								: null
				)
				.datasetName(
						rule.getColumn() != null && rule.getColumn().getDataset() != null
								? rule.getColumn().getDataset().getDatasetName()
								: null
				)
				.build();
	}

	private String getRuleTypeLabel(RuleType ruleType) {
		if (ruleType == null)
			return null;
		return switch (ruleType) {
			case TYPE_CONVERSION -> "Type conversion";
			case DUPLICATE_REMOVAL -> "Duplicate removal";
			case NULL_IMPUTATION -> "Null imputation";
			case REGEX_CLEANING -> "Regex cleaning";
			case TRIM -> "Trim";
			case VALUE_CONSTRAINT -> "Value constraint";
		};
	}
}
