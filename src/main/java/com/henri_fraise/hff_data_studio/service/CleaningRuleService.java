package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CleaningRuleRequest;
import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.CleaningRuleMapper;
import com.henri_fraise.hff_data_studio.repository.CleaningRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningRuleService {

	private final CleaningRuleRepository ruleRepository;
	private final CleaningRuleMapper ruleMapper;
	private final DatasetColumnService columnService;

	@Transactional
	public CleaningRule createRule(UUID columnId, CleaningRuleRequest request) {
		DatasetColumn column = columnService.getColumnEntityById(columnId);
		CleaningRule rule = ruleMapper.toEntity(request, column);
		CleaningRule saved = ruleRepository.save(rule);
		log.info("Cleaning rule created: {} for column: {}", saved.getId(), column.getOriginalName());
		return saved;
	}

	public CleaningRule getRuleEntityById(UUID ruleId) {
		return ruleRepository.findById(ruleId)
				.orElseThrow(() -> new ResourceNotFoundException("Cleaning rule not found: " + ruleId));
	}

	public CleaningRuleResponse getRuleById(UUID ruleId) {
		CleaningRule rule = getRuleEntityById(ruleId);
		return ruleMapper.toResponse(rule);
	}

	public List<CleaningRuleResponse> getRulesByColumn(UUID columnId) {
		List<CleaningRule> rules = ruleRepository.findByColumnIdOrderByExecutionOrderAsc(columnId);
		return rules.stream().map(ruleMapper::toResponse).toList();
	}

	public List<CleaningRule> getActiveRulesByColumn(UUID columnId) {
		return ruleRepository.findByColumnIdAndIsActiveTrueOrderByExecutionOrderAsc(columnId);
	}

	public List<CleaningRule> getActiveRulesByDataset(UUID datasetId) {
		return ruleRepository.findActiveRulesByDatasetIdOrdered(datasetId);
	}

	public List<CleaningRule> getAllRulesByDataset(UUID datasetId) {
		return ruleRepository.findRulesByDatasetIdOrdered(datasetId);
	}

	public List<CleaningRule> getRulesByRuleType(RuleType ruleType) {
		return ruleRepository.findByRuleType(ruleType);
	}

	public List<CleaningRule> getRulesByColumnAndType(UUID columnId, RuleType ruleType) {
		return ruleRepository.findByColumnIdAndRuleType(columnId, ruleType);
	}

	public List<CleaningRule> getRulesByDatasetAndType(UUID datasetId, RuleType ruleType) {
		return ruleRepository.findByDatasetIdAndRuleType(datasetId, ruleType);
	}

	@Transactional
	public CleaningRule updateRule(UUID ruleId, CleaningRuleRequest request) {
		CleaningRule rule = getRuleEntityById(ruleId);
		ruleMapper.updateEntity(rule, request);
		CleaningRule updated = ruleRepository.save(rule);
		log.info("Cleaning rule updated: {}", ruleId);
		return updated;
	}

	@Transactional
	public void deleteRule(UUID ruleId) {
		CleaningRule rule = getRuleEntityById(ruleId);
		ruleRepository.delete(rule);
		log.info("Cleaning rule deleted: {}", ruleId);
	}

	@Transactional
	public void deleteRulesByColumn(UUID columnId) {
		List<CleaningRule> rules = ruleRepository.findByColumnIdOrderByExecutionOrderAsc(columnId);
		ruleRepository.deleteAll(rules);
		log.info("Deleted {} rules for column: {}", rules.size(), columnId);
	}

	@Transactional
	public void toggleRuleActive(UUID ruleId, boolean active) {
		CleaningRule rule = getRuleEntityById(ruleId);
		rule.setIsActive(active);
		ruleRepository.save(rule);
		log.info("Rule {} active status: {}", ruleId, active);
	}

	@Transactional
	public void deactivateAllRulesForColumn(UUID columnId) {
		ruleRepository.deactivateAllRulesForColumn(columnId);
		log.info("Deactivated all rules for column: {}", columnId);
	}

	@Transactional
	public void reorderRules(UUID columnId, List<UUID> ruleIds) {
		for (int i = 0; i < ruleIds.size(); i++) {
			CleaningRule rule = getRuleEntityById(ruleIds.get(i));
			rule.setExecutionOrder(i + 1);
			ruleRepository.save(rule);
		}
		log.info("Reordered {} rules for column: {}", ruleIds.size(), columnId);
	}

	@Transactional
	public void updateExecutionOrder(UUID ruleId, Integer order) {
		ruleRepository.updateExecutionOrder(ruleId, order);
		log.info("Updated execution order for rule: {} to {}", ruleId, order);
	}

	@Transactional
	public void updateRuleStatus(UUID ruleId, Boolean isActive) {
		ruleRepository.updateRuleStatus(ruleId, isActive);
		log.info("Updated status for rule: {} to {}", ruleId, isActive);
	}

	public long countRulesByColumn(UUID columnId) {
		return ruleRepository.countByColumnId(columnId);
	}

	public long countActiveRulesByColumn(UUID columnId) {
		return ruleRepository.countByColumnIdAndIsActiveTrue(columnId);
	}

	public long countRulesByDataset(UUID datasetId) {
		return ruleRepository.countByColumn_DatasetId(datasetId);
	}

	public Integer getMaxExecutionOrderByDataset(UUID datasetId) {
		return ruleRepository.findMaxExecutionOrderByDatasetId(datasetId);
	}

	public List<CleaningRule> getRulesByColumnEntity(DatasetColumn column) {
		return ruleRepository.findByColumnIdOrderByExecutionOrderAsc(column.getId());
	}

	public boolean existsById(UUID ruleId) {
		return ruleRepository.existsById(ruleId);
	}

	public List<CleaningRule> getRulesByExecutionOrder(UUID datasetId, Integer order) {
		return ruleRepository.findByDatasetIdAndExecutionOrder(datasetId, order);
	}
}