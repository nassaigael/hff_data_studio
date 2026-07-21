package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CleaningRuleRequest;
import com.henri_fraise.hff_data_studio.dto.response.CleaningRuleResponse;
import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.CleaningRuleMapper;
import com.henri_fraise.hff_data_studio.repository.CleaningRuleRepository;
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
public class CleaningRuleService {

  private final CleaningRuleRepository ruleRepository;
  private final CleaningRuleMapper ruleMapper;
  private final DatasetColumnService columnService;
  private final AuditLogService auditLogService;

  @Transactional
  public CleaningRule createRule(UUID columnId, CleaningRuleRequest request) {
    try {
      DatasetColumn column = columnService.getColumnEntityById(columnId);
      CleaningRule rule = ruleMapper.toEntity(request, column);
      CleaningRule saved = ruleRepository.save(rule);

      log.info("Cleaning rule created: {} for column: {}", saved.getId(), column.getOriginalName());

      auditLogService.logAction(
          "CLEANING_RULE_CREATED",
          "CleaningRule",
          saved.getId(),
          "Cleaning rule created for column: " + column.getOriginalName());

      return saved;
    } catch (Exception ex) {
      log.error("Error creating cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create cleaning rule", ex);
    }
  }

  public CleaningRule getRuleEntityById(UUID ruleId) {
    return ruleRepository
        .findById(ruleId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Cleaning rule not found with id: " + ruleId));
  }

  public CleaningRuleResponse getRuleById(UUID ruleId) {
    CleaningRule rule = getRuleEntityById(ruleId);
    return ruleMapper.toResponse(rule);
  }

  public List<CleaningRuleResponse> getRulesByColumn(UUID columnId) {
    try {
      List<CleaningRule> rules = ruleRepository.findByColumnIdOrderByExecutionOrderAsc(columnId);
      return rules.stream().map(ruleMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error getting rules by column: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rules by column", ex);
    }
  }

  public List<CleaningRule> getActiveRulesByColumn(UUID columnId) {
    try {
      return ruleRepository.findByColumnIdAndIsActiveTrueOrderByExecutionOrderAsc(columnId);
    } catch (Exception ex) {
      log.error("Error getting active rules by column: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get active rules by column", ex);
    }
  }

  public List<CleaningRule> getActiveRulesByDataset(UUID datasetId) {
    try {
      return ruleRepository.findActiveRulesByDatasetIdOrdered(datasetId);
    } catch (Exception ex) {
      log.error("Error getting active rules by dataset: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get active rules by dataset", ex);
    }
  }

  public List<CleaningRule> getAllRulesByDataset(UUID datasetId) {
    try {
      return ruleRepository.findRulesByDatasetIdOrdered(datasetId);
    } catch (Exception ex) {
      log.error("Error getting all rules by dataset: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get all rules by dataset", ex);
    }
  }

  public List<CleaningRule> getRulesByRuleType(RuleType ruleType) {
    try {
      return ruleRepository.findByRuleType(ruleType);
    } catch (Exception ex) {
      log.error("Error getting rules by rule type: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rules by rule type", ex);
    }
  }

  public List<CleaningRule> getRulesByColumnAndType(UUID columnId, RuleType ruleType) {
    try {
      return ruleRepository.findByColumnIdAndRuleType(columnId, ruleType);
    } catch (Exception ex) {
      log.error("Error getting rules by column and type: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rules by column and type", ex);
    }
  }

  public List<CleaningRule> getRulesByDatasetAndType(UUID datasetId, RuleType ruleType) {
    try {
      return ruleRepository.findByDatasetIdAndRuleType(datasetId, ruleType);
    } catch (Exception ex) {
      log.error("Error getting rules by dataset and type: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rules by dataset and type", ex);
    }
  }

  public List<CleaningRule> getActiveRulesByDatasetAndType(UUID datasetId, RuleType ruleType) {
    try {
      return ruleRepository.findActiveRulesByDatasetIdAndRuleType(datasetId, ruleType);
    } catch (Exception ex) {
      log.error("Error getting active rules by dataset and type: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get active rules by dataset and type", ex);
    }
  }

  @Transactional
  public CleaningRule updateRule(UUID ruleId, CleaningRuleRequest request) {
    try {
      CleaningRule rule = getRuleEntityById(ruleId);
      ruleMapper.updateEntity(rule, request);
      CleaningRule updated = ruleRepository.save(rule);

      log.info("Cleaning rule updated: {}", ruleId);

      auditLogService.logAction(
          "CLEANING_RULE_UPDATED", "CleaningRule", ruleId, "Cleaning rule updated");

      return updated;
    } catch (Exception ex) {
      log.error("Error updating cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update cleaning rule", ex);
    }
  }

  @Transactional
  public void toggleRuleActive(UUID ruleId, boolean active) {
    try {
      CleaningRule rule = getRuleEntityById(ruleId);
      rule.setIsActive(active);
      ruleRepository.save(rule);
      log.info("Rule {} active status: {}", ruleId, active);

      auditLogService.logAction(
          "CLEANING_RULE_TOGGLED",
          "CleaningRule",
          ruleId,
          "Cleaning rule active status changed to: " + active);
    } catch (Exception ex) {
      log.error("Error toggling rule active status: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to toggle rule active status", ex);
    }
  }

  @Transactional
  public void deactivateAllRulesForColumn(UUID columnId) {
    try {
      ruleRepository.deactivateAllRulesForColumn(columnId);
      log.info("Deactivated all rules for column: {}", columnId);
    } catch (Exception ex) {
      log.error("Error deactivating all rules for column: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to deactivate all rules for column", ex);
    }
  }

  @Transactional
  public void reorderRules(UUID columnId, List<UUID> ruleIds) {
    try {
      for (int i = 0; i < ruleIds.size(); i++) {
        CleaningRule rule = getRuleEntityById(ruleIds.get(i));
        rule.setExecutionOrder(i + 1);
        ruleRepository.save(rule);
      }
      log.info("Reordered {} rules for column: {}", ruleIds.size(), columnId);
    } catch (Exception ex) {
      log.error("Error reordering rules: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to reorder rules", ex);
    }
  }

  @Transactional
  public void updateExecutionOrder(UUID ruleId, Integer order) {
    try {
      ruleRepository.updateExecutionOrder(ruleId, order);
      log.info("Updated execution order for rule: {} to {}", ruleId, order);
    } catch (Exception ex) {
      log.error("Error updating execution order: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update execution order", ex);
    }
  }

  @Transactional
  public void updateRuleStatus(UUID ruleId, Boolean isActive) {
    try {
      ruleRepository.updateRuleStatus(ruleId, isActive);
      log.info("Updated status for rule: {} to {}", ruleId, isActive);
    } catch (Exception ex) {
      log.error("Error updating rule status: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update rule status", ex);
    }
  }

  @Transactional
  public void deleteRule(UUID ruleId) {
    try {
      CleaningRule rule = getRuleEntityById(ruleId);
      ruleRepository.delete(rule);
      log.info("Cleaning rule deleted: {}", ruleId);

      auditLogService.logAction(
          "CLEANING_RULE_DELETED", "CleaningRule", ruleId, "Cleaning rule deleted");
    } catch (Exception ex) {
      log.error("Error deleting cleaning rule: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete cleaning rule", ex);
    }
  }

  @Transactional
  public void deleteRulesByColumn(UUID columnId) {
    try {
      List<CleaningRule> rules = ruleRepository.findByColumnIdOrderByExecutionOrderAsc(columnId);
      ruleRepository.deleteAll(rules);
      log.info("Deleted {} rules for column: {}", rules.size(), columnId);
    } catch (Exception ex) {
      log.error("Error deleting rules by column: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete rules by column", ex);
    }
  }

  @Transactional
  public void deleteRulesByDataset(UUID datasetId) {
    try {
      List<CleaningRule> rules = ruleRepository.findRulesByDatasetIdOrdered(datasetId);
      ruleRepository.deleteAll(rules);
      log.info("Deleted {} rules for dataset: {}", rules.size(), datasetId);
    } catch (Exception ex) {
      log.error("Error deleting rules by dataset: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete rules by dataset", ex);
    }
  }

  // ==================== COUNT METHODS ====================

  public long countRulesByColumn(UUID columnId) {
    try {
      return ruleRepository.countByColumnId(columnId);
    } catch (Exception ex) {
      log.error("Error counting rules by column: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public long countActiveRulesByColumn(UUID columnId) {
    try {
      return ruleRepository.countByColumnIdAndIsActiveTrue(columnId);
    } catch (Exception ex) {
      log.error("Error counting active rules by column: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public long countRulesByDataset(UUID datasetId) {
    try {
      return ruleRepository.countByColumn_DatasetId(datasetId);
    } catch (Exception ex) {
      log.error("Error counting rules by dataset: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public long countActiveRulesByDataset(UUID datasetId) {
    try {
      return ruleRepository.countActiveRulesByDatasetId(datasetId);
    } catch (Exception ex) {
      log.error("Error counting active rules by dataset: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public long countRulesByType(RuleType ruleType) {
    try {
      return ruleRepository.countByRuleType(ruleType);
    } catch (Exception ex) {
      log.error("Error counting rules by type: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public long countAllRules() {
    try {
      return ruleRepository.count();
    } catch (Exception ex) {
      log.error("Error counting all rules: {}", ex.getMessage(), ex);
      return 0L;
    }
  }

  public boolean existsById(UUID ruleId) {
    return ruleRepository.existsById(ruleId);
  }

  public boolean hasActiveRules(UUID datasetId) {
    return countActiveRulesByDataset(datasetId) > 0;
  }

  public boolean hasRulesByColumn(UUID columnId) {
    return countRulesByColumn(columnId) > 0;
  }

  public Integer getMaxExecutionOrderByDataset(UUID datasetId) {
    try {
      return ruleRepository.findMaxExecutionOrderByDatasetId(datasetId);
    } catch (Exception ex) {
      log.error("Error getting max execution order: {}", ex.getMessage(), ex);
      return null;
    }
  }

  public List<CleaningRule> getRulesByColumnEntity(DatasetColumn column) {
    return ruleRepository.findByColumnIdOrderByExecutionOrderAsc(column.getId());
  }

  public List<CleaningRule> getRulesByExecutionOrder(UUID datasetId, Integer order) {
    try {
      return ruleRepository.findByDatasetIdAndExecutionOrder(datasetId, order);
    } catch (Exception ex) {
      log.error("Error getting rules by execution order: {}", ex.getMessage(), ex);
      return List.of();
    }
  }

  public List<CleaningRule> getRulesByDatasetOrdered(UUID datasetId) {
    try {
      return ruleRepository.findRulesByDatasetIdOrdered(datasetId);
    } catch (Exception ex) {
      log.error("Error getting rules by dataset ordered: {}", ex.getMessage(), ex);
      return List.of();
    }
  }

  @Transactional
  public void activateRulesBulk(List<UUID> ruleIds) {
    try {
      for (UUID ruleId : ruleIds) {
        toggleRuleActive(ruleId, true);
      }
      log.info("Activated {} rules in bulk", ruleIds.size());
    } catch (Exception ex) {
      log.error("Error activating rules in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to activate rules in bulk", ex);
    }
  }

  @Transactional
  public void deactivateRulesBulk(List<UUID> ruleIds) {
    try {
      for (UUID ruleId : ruleIds) {
        toggleRuleActive(ruleId, false);
      }
      log.info("Deactivated {} rules in bulk", ruleIds.size());
    } catch (Exception ex) {
      log.error("Error deactivating rules in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to deactivate rules in bulk", ex);
    }
  }

  @Transactional
  public void deleteRulesBulk(List<UUID> ruleIds) {
    try {
      for (UUID ruleId : ruleIds) {
        deleteRule(ruleId);
      }
      log.info("Deleted {} rules in bulk", ruleIds.size());
    } catch (Exception ex) {
      log.error("Error deleting rules in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete rules in bulk", ex);
    }
  }

  public Map<String, Object> getRuleStatistics(UUID datasetId) {
    Map<String, Object> stats = new HashMap<>();
    try {
      stats.put("totalRules", countRulesByDataset(datasetId));
      stats.put("activeRules", countActiveRulesByDataset(datasetId));

      for (RuleType type : RuleType.values()) {
        long count = ruleRepository.countByDatasetIdAndRuleType(datasetId, type);
        stats.put(type.name().toLowerCase() + "Count", count);
      }

      return stats;
    } catch (Exception ex) {
      log.error("Error getting rule statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get rule statistics", ex);
    }
  }
}
