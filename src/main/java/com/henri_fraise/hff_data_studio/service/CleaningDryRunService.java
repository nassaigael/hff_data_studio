package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DryRunResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import com.henri_fraise.hff_data_studio.repository.CleaningRuleRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningDryRunService {

	private final CleaningRuleRepository ruleRepository;
	private final DatasetService datasetService;
	private final SecurityUtils securityUtils;

	@Transactional(readOnly = true)
	public DryRunResponse dryRun(UUID datasetId, List<UUID> ruleIds) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		List<CleaningRule> rules;

		if (ruleIds == null || ruleIds.isEmpty()) {
			rules = ruleRepository.findActiveRulesByDatasetIdOrdered(datasetId);
		} else {
			rules = ruleIds.stream()
					.map(id -> ruleRepository.findById(id).orElse(null))
					.filter(r -> r != null)
					.toList();
		}

		List<DryRunResponse.RulePreview> previews = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		int totalAffectedRows = 0;
		int affectedColumns = 0;
		long estimatedDuration = 0L;

		for (CleaningRule rule : rules) {
			DryRunResponse.RulePreview preview = simulateRule(dataset, rule);
			previews.add(preview);
			totalAffectedRows += preview.getEstimatedAffectedRows();
			if (preview.getEstimatedAffectedRows() > 0) affectedColumns++;
			if (preview.getWarning() != null) warnings.add(preview.getWarning());
			estimatedDuration += estimateDuration(rule.getRuleType(), dataset.getRowCount());
		}

		return DryRunResponse.builder()
				.datasetId(datasetId)
				.totalRules(rules.size())
				.affectedRows(totalAffectedRows)
				.affectedColumns(affectedColumns)
				.estimatedDurationMs(estimatedDuration)
				.previews(previews)
				.canProceed(warnings.isEmpty())
				.warnings(warnings)
				.build();
	}

	private DryRunResponse.RulePreview simulateRule(Dataset dataset, CleaningRule rule) {
		DatasetColumn column = rule.getColumn();
		int affected = 0;
		String warning = null;
		Map<String, Object> changes = new HashMap<>();

		switch (rule.getRuleType()) {
			case TYPE_CONVERSION -> affected = dataset.getRowCount();
			case DUPLICATE_REMOVAL -> affected = 0;
			case NULL_IMPUTATION -> {
				if (column != null) affected = column.getNullCount();
			}
			case REGEX_CLEANING -> affected = 0;
			case TRIM -> affected = 0;
			case VALUE_CONSTRAINT -> {
				if (column != null) affected = column.getNullCount();
			}
		}

		if (column == null && rule.getRuleType() != RuleType.DUPLICATE_REMOVAL) {
			warning = "Rule " + rule.getId() + " has no target column";
		}

		if (dataset.getRowCount() > 500000) {
			warning = "Large dataset detected, execution may take a while";
		}

		changes.put("ruleType", rule.getRuleType().name());
		changes.put("executionOrder", rule.getExecutionOrder());

		return DryRunResponse.RulePreview.builder()
				.ruleId(rule.getId())
				.ruleType(rule.getRuleType().name())
				.columnName(column != null ? column.getOriginalName() : null)
				.estimatedAffectedRows(affected)
				.sampleBefore(affected > 0 ? 1 : 0)
				.sampleAfter(0)
				.sampleChanges(changes)
				.warning(warning)
				.build();
	}

	private long estimateDuration(RuleType ruleType, int rowCount) {
		long baseMs = switch (ruleType) {
			case TYPE_CONVERSION -> 1L;
			case DUPLICATE_REMOVAL -> 3L;
			case NULL_IMPUTATION -> 2L;
			case REGEX_CLEANING -> 5L;
			case TRIM -> 1L;
			case VALUE_CONSTRAINT -> 2L;
		};
		return baseMs * rowCount / 1000;
	}
}