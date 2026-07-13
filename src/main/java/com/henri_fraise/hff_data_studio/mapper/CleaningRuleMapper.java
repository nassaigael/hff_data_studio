package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.stereotype.Component;

@Component
public class CleaningRuleMapper {

	public CleaningRuleResponse toResponse(CleaningRule rule) {
		if (rule == null) return null;

		RuleType ruleType = rule.getRuleType();
		DatasetColumn column = rule.getColumn();
		Dataset dataset = getDataset(column);

		return CleaningRuleResponse.builder().ruleId(rule.getId()).ruleType(ruleType).ruleTypeLabel(getRuleTypeLabel(ruleType)).parametersJson(rule.getParametersJson()).executionOrder(rule.getExecutionOrder()).isActive(rule.getIsActive()).columnId(column != null ? column.getId() : null).columnName(column != null ? column.getOriginalName() : null).datasetId(dataset != null ? dataset.getId() : null).datasetName(dataset != null ? dataset.getDatasetName() : null).build();
	}

	private Dataset getDataset(DatasetColumn column) {
		return column != null ? column.getDataset() : null;
	}

	private String getRuleTypeLabel(RuleType ruleType) {
		if (ruleType == null) return null;
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